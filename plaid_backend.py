import os
import json
from datetime import datetime, timedelta
from flask import Flask, request, jsonify
import plaid
from plaid.model.link_token_create_request import LinkTokenCreateRequest
from plaid.model.link_token_create_request_user import LinkTokenCreateRequestUser
from plaid.model.item_public_token_exchange_request import ItemPublicTokenExchangeRequest
from plaid.model.transactions_get_request import TransactionsGetRequest
from plaid.model.accounts_get_request import AccountsGetRequest
from plaid.model.item_remove_request import ItemRemoveRequest
from plaid.model.institutions_get_by_id_request import InstitutionsGetByIdRequest
from plaid.model.products import Products
from plaid.model.country_code import CountryCode
from plaid.api import plaid_api

app = Flask(__name__)

@app.route('/')
def home():
    return "Financial App Backend is Running (Stateless Mode)!"

# --- CONFIGURATION ---
PLAID_CLIENT_ID = '6a0928409114fc000d01c3df'
PLAID_SECRET = '3a74871d669639ff27c2648f38bc7e'
PLAID_ENV = 'production'
# ---------------------

plaid_host = plaid.Environment.Sandbox if PLAID_ENV == 'sandbox' else plaid.Environment.Production
configuration = plaid.Configuration(
    host=plaid_host,
    api_key={'clientId': PLAID_CLIENT_ID, 'secret': PLAID_SECRET}
)
api_client = plaid.ApiClient(configuration)
client = plaid_api.PlaidApi(api_client)

@app.route('/api/link_token', methods=['GET'])
def get_link_token():
    print("DEBUG: Generating link token for transactions and auth...")
    try:
        # Explicitly using 'transactions' and 'auth' for maximum bank compatibility (BofA, Chase, etc.)
        # This removes the investment check that was causing errors.
        request_params = LinkTokenCreateRequest(
            products=[Products('transactions'), Products('auth')],
            client_name="Financial Transactions App",
            country_codes=[CountryCode('US')],
            language='en',
            user=LinkTokenCreateRequestUser(client_user_id='user-123'),
            android_package_name='com.example.financialtransactions'
        )
        response = client.link_token_create(request_params)
        return jsonify({'link_token': response.link_token})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/exchange_public_token', methods=['POST'])
def exchange_token():
    data = request.get_json()
    public_token = data.get('public_token')
    try:
        exchange_request = ItemPublicTokenExchangeRequest(public_token=public_token)
        exchange_response = client.item_public_token_exchange(exchange_request)

        # Return tokens to the phone, backend DOES NOT store them
        return jsonify({
            'success': True,
            'access_token': exchange_response.access_token,
            'item_id': exchange_response.item_id
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/accounts', methods=['POST'])
def get_accounts():
    data = request.get_json()
    access_tokens = data.get('access_tokens', [])
    unique_accounts = {}

    for token in access_tokens:
        try:
            request_params = AccountsGetRequest(access_token=token)
            response = client.accounts_get(request_params)

            # Fetch Institution Name
            institution_name = "Linked Bank"
            try:
                ins_id = response.item.institution_id
                ins_request = InstitutionsGetByIdRequest(
                    institution_id=ins_id,
                    country_codes=[CountryCode('US')]
                )
                ins_response = client.institutions_get_by_id(ins_request)
                institution_name = ins_response.institution.name
            except:
                pass

            for acc in response.accounts:
                if acc.account_id in unique_accounts:
                    continue

                p_type = str(acc.type).lower()
                p_subtype = str(acc.subtype).lower() if acc.subtype else ""

                app_type = "CHECKING"
                if p_type == "credit":
                    app_type = "CREDIT_CARD"
                elif p_type == "investment":
                    app_type = "INVESTMENT"
                elif p_subtype == "savings":
                    app_type = "SAVINGS"

                unique_accounts[acc.account_id] = {
                    "id": acc.account_id,
                    "name": acc.name,
                    "institution": institution_name,
                    "balance": float(acc.balances.current or 0),
                    "type": app_type
                }
        except Exception as e:
            print(f"Error fetching accounts: {e}")

    return jsonify(list(unique_accounts.values()))

@app.route('/api/transactions', methods=['POST'])
def get_transactions():
    data = request.get_json()
    access_tokens = data.get('access_tokens', [])
    unique_txns = {}

    start_date = (datetime.now() - timedelta(days=30)).date()
    end_date = datetime.now().date()

    for token in access_tokens:
        try:
            request_params = TransactionsGetRequest(
                access_token=token,
                start_date=start_date,
                end_date=end_date
            )
            response = client.transactions_get(request_params)

            # Fetch Institution Name for this token
            institution_name = "Bank"
            try:
                ins_id = response.item.institution_id
                ins_request = InstitutionsGetByIdRequest(
                    institution_id=ins_id,
                    country_codes=[CountryCode('US')]
                )
                ins_response = client.institutions_get_by_id(ins_request)
                institution_name = ins_response.institution.name
            except:
                pass

            for txn in response.transactions:
                if txn.transaction_id in unique_txns:
                    continue

                try:
                    txn_date = txn.date
                    if isinstance(txn_date, str):
                        txn_date = datetime.strptime(txn_date, '%Y-%m-%d').date()
                    date_str = txn_date.strftime('%Y-%m-%d')

                    unique_txns[txn.transaction_id] = {
                        "id": txn.transaction_id,
                        "accountId": txn.account_id,
                        "amount": float(txn.amount),
                        "merchant": txn.merchant_name or txn.name or "Unknown Merchant",
                        "date": date_str,
                        "category": txn.category[0] if (txn.category and len(txn.category) > 0) else "General",
                        "institution": institution_name
                    }
                except Exception as inner_e:
                    print(f"Error parsing transaction: {inner_e}")

        except Exception as e:
            print(f"Error fetching transactions for token: {e}")

    result = list(unique_txns.values())
    result.sort(key=lambda x: x['date'], reverse=True)
    return jsonify(result)

@app.route('/api/unlink_account', methods=['POST'])
def unlink_account():
    data = request.get_json()
    token = data.get('access_token')
    try:
        if token:
            client.item_remove(ItemRemoveRequest(access_token=token))
        return jsonify({'success': True})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 8000))
    app.run(host='0.0.0.0', port=port)

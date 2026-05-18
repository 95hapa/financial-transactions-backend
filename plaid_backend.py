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
from plaid.model.products import Products
from plaid.model.country_code import CountryCode
from plaid.api import plaid_api

app = Flask(__name__)

@app.route('/')
def home():
    return "Financial App Backend is Running!"

# --- CONFIGURATION ---
PLAID_CLIENT_ID = '6a0928409114fc000d01c3df'
PLAID_SECRET = '3a74871d669639ff27c2648f38bc7e'
PLAID_ENV = 'production' # Plaid removed 'development' in June 2024
# ---------------------

plaid_host = plaid.Environment.Sandbox if PLAID_ENV == 'sandbox' else plaid.Environment.Production
configuration = plaid.Configuration(
    host=plaid_host,
    api_key={'clientId': PLAID_CLIENT_ID, 'secret': PLAID_SECRET}
)
api_client = plaid.ApiClient(configuration)
client = plaid_api.PlaidApi(api_client)

# In-memory token storage (Use a DB for production!)
access_token = None

@app.route('/api/link_token', methods=['GET'])
def get_link_token():
    try:
        request_params = LinkTokenCreateRequest(
            products=[Products('transactions')],
            client_name="Financial Transactions App",
            country_codes=[CountryCode('US')],
            language='en',
            user=LinkTokenCreateRequestUser(client_user_id='user-123'),
            android_package_name='com.example.financialtransactions'
        )
        response = client.link_token_create(request_params)
        return jsonify({'link_token': response.link_token})
    except Exception as e:
        print(f"DEBUG: Link Token Error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/exchange_public_token', methods=['POST'])
def exchange_token():
    global access_token
    data = request.get_json()
    public_token = data.get('public_token')
    try:
        exchange_request = ItemPublicTokenExchangeRequest(public_token=public_token)
        exchange_response = client.item_public_token_exchange(exchange_request)
        access_token = exchange_response.access_token
        print(f"DEBUG: Account linked! Token starting with: {access_token[:10]}...")
        return jsonify({'success': True})
    except Exception as e:
        print(f"DEBUG: Exchange Error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/accounts', methods=['GET'])
def get_accounts():
    if not access_token: return jsonify([])
    try:
        request_params = AccountsGetRequest(access_token=access_token)
        response = client.accounts_get(request_params)

        # Using attribute access (.) instead of dictionary access ([]) for stability
        return jsonify([{
            "id": acc.account_id,
            "name": acc.name,
            "institution": "Linked Bank",
            "balance": float(acc.balances.current or 0),
            "type": "CHECKING"
        } for acc in response.accounts])
    except Exception as e:
        print(f"DEBUG: Get Accounts Error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/transactions', methods=['GET'])
def get_transactions():
    if not access_token: return jsonify([])
    try:
        start_date = (datetime.now() - timedelta(days=30)).date()
        end_date = datetime.now().date()
        request_params = TransactionsGetRequest(
            access_token=access_token,
            start_date=start_date,
            end_date=end_date
        )
        response = client.transactions_get(request_params)

        txns = []
        for txn in response.transactions:
            # Safely handle the date conversion
            txn_date = txn.date
            if isinstance(txn_date, str):
                txn_date = datetime.strptime(txn_date, '%Y-%m-%d').date()

            timestamp = int(datetime.combine(txn_date, datetime.min.time()).timestamp() * 1000)

            txns.append({
                "id": txn.transaction_id,
                "amount": float(txn.amount),
                "merchant": txn.merchant_name or txn.name,
                "date": timestamp,
                "accountName": "Linked Account",
                "category": txn.category[0] if txn.category else "General",
                "institution": "Bank"
            })
        return jsonify(txns)
    except Exception as e:
        print(f"DEBUG: Get Transactions Error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/unlink_account', methods=['POST'])
def unlink_account():
    global access_token
    try:
        if access_token:
            client.item_remove(ItemRemoveRequest(access_token=access_token))
            access_token = None
        return jsonify({'success': True})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 8000))
    app.run(host='0.0.0.0', port=port)

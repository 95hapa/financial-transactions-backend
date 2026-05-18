import http.server
import json
import time

class MockPlaidHandler(http.server.BaseHTTPRequestHandler):
    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_GET(self):
        if self.path == '/api/link_token':
            self._set_headers()
            response = {"link_token": "link-sandbox-12345678-1234-1234-1234-123456789012"}
            self.wfile.write(json.dumps(response).encode())

        elif self.path == '/api/transactions':
            self._set_headers()
            transactions = [
                {"id": "1", "amount": -15.50, "merchant": "Uber", "date": int(time.time() * 1000), "accountName": "Credit", "category": "Travel", "institution": "Capital One"},
                {"id": "2", "amount": -200.00, "merchant": "Whole Foods", "date": int(time.time() * 1000), "accountName": "Checking", "category": "Groceries", "institution": "Chase"},
                {"id": "3", "amount": -5.25, "merchant": "Starbucks", "date": int(time.time() * 1000), "accountName": "Checking", "category": "Food", "institution": "Chase"},
            ]
            self.wfile.write(json.dumps(transactions).encode())
        else:
            self.send_error(404)

    def do_POST(self):
        if self.path == '/api/exchange_public_token':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode())
            print(f"Received public token: {data.get('public_token')}")

            self._set_headers()
            self.wfile.write(json.dumps({"success": True}).encode())
        else:
            self.send_error(404)

def run(server_class=http.server.HTTPServer, handler_class=MockPlaidHandler, port=8000):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print(f'Starting mock backend on port {port}...')
    print(f'Accessible from Android Emulator at http://10.0.2.2:{port}')
    httpd.serve_forever()

if __name__ == "__main__":
    run()

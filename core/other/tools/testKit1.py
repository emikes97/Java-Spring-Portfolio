#!/usr/bin/env python3
import time, random, uuid
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests
import psycopg2
import psycopg2.extras

# =========================
# Constants
# =========================
BASE_URL    = "http://localhost:8080"
CONCURRENCY = 8

ADD_TO_CART = "/api/v1/customers/{cid}/cart"
CHECKOUT    = "/api/v1/customers/{cid}/checkout"
PAY_ORDER = "/api/v1/payout/{cid}/order/{oid}"

PG = dict(host="localhost", port=5432, dbname="eshop", user="admin", password="admin")

# orders.* — adjust if your column names differ
ORDERS_TABLE    = "orders"
ORDER_ID_COL    = "order_id"
CUSTOMER_ID_COL = "customer_id"
CREATED_AT_COL  = "order_created_at"    # <- your schema uses this
OUTSTANDING_COL = "total_outstanding"

PRODUCTS_TABLE  = "products"
PRODUCT_ID_COL  = "product_id"

TX_CURRENCY = "EUR"

# one constant card used for everyone (matches UseNewCard)
PAYMENT_NEW_CARD = {
    "type": "USE_NEW_CARD",
    "panMasked": "411111******1111",
    "brand": "VISA",
    "expMonth": 12,
    "expYear": 2030,
    "holderName": "Load Test",
    "cvc": "123"
}

CUSTOMERS = [
  "1dfbf8d3-419d-5beb-8c07-0c28fabee34b","359e7038-7f63-5f0c-97a5-c024defa2c3d","d6277657-f105-50e9-b59f-81b370626aa6",
  "8bb5dbc9-3fb1-5d7f-8c9d-2279c5761467","00251527-2f24-56c8-ae55-4540d7873bd7","ac259b3c-bf38-5b30-a201-eb35a46064fd",
  "1bd28b79-182d-56ca-b6b3-743b1b610404","b38b13e3-f599-59d6-9efc-bc3c7084ae41","9892810f-9448-516d-b184-60163d511d6c",
  "ef93248f-9e98-5475-92a0-8f6e2082f978","a84ffe3f-10c3-53f4-8eb7-947f78900ed0","d144cb9a-01e7-55de-90ca-eaa7c09aab68",
  "a61ea36e-f736-586e-9672-c11847c8aa75","fc0216cd-45cf-51bc-9a88-440bf499bd87","a1075f31-73d0-5d88-a985-16ecebe77c74",
  "a65463f5-6488-5ad5-aefc-2940e9d913a0","8d154520-e2b9-5530-ba50-14489207c4cf","73bab0df-1d56-557e-840c-9dfb9ef7ecac",
  "6a27c30a-fbb7-5907-a410-37299006b3d9","c0f91d4c-2fe8-5b6e-bd0c-917e673aabab",
]

# =========================
# Helpers
# =========================
def db_conn():
    conn = psycopg2.connect(**PG)
    psycopg2.extras.register_uuid(conn_or_curs=conn)
    return conn

def fetch_all_product_ids(cur):
    cur.execute(f"SELECT {PRODUCT_ID_COL} FROM {PRODUCTS_TABLE};")
    return [row[0] for row in cur.fetchall()]

def random_address():
    streets = ["Akadimias", "Stadiou", "Panepistimiou", "Ermou", "Patission", "Syggrou"]
    cities  = ["Athens", "Piraeus", "Thessaloniki", "Larissa", "Heraklion"]
    street  = f"{random.choice(streets)} {random.randint(1, 250)}"
    postal  = f"{random.randint(10_000, 99_999)}"
    return {"country": "GR", "street": street, "city": random.choice(cities), "postalCode": postal}

def build_transaction_request(amount):
    # Send UseNewCard as the payment instruction inside your DTOTransactionRequest
    return {
        "amount": float(amount),            # keep it simple for JSON
        "currency": TX_CURRENCY,
        "paymentInstruction": PAYMENT_NEW_CARD
    }

def add_random_items(session, customer_id, product_ids):
    adds = random.randint(1, 5)
    picks = random.sample(product_ids, k=adds)
    for pid in picks:
        qty = random.randint(1, 3)
        payload = {"productId": pid, "quantity": qty}
        r = session.post(BASE_URL + ADD_TO_CART.format(cid=customer_id), json=payload, timeout=20)
        r.raise_for_status()

def checkout(session, customer_id):
    payload = random_address()
    r = session.post(BASE_URL + CHECKOUT.format(cid=customer_id), json=payload, timeout=30)
    r.raise_for_status()
    return r.json() if r.headers.get("content-type","").startswith("application/json") else None

def fetch_latest_order_for_customer(cur, customer_id, retries=10, delay=0.3):
    sql = f"""
      SELECT {ORDER_ID_COL}, {OUTSTANDING_COL}
      FROM {ORDERS_TABLE}
      WHERE {CUSTOMER_ID_COL} = %s::uuid
      ORDER BY {CREATED_AT_COL} DESC
      LIMIT 1;
    """
    for _ in range(retries):
        cur.execute(sql, (customer_id,))
        row = cur.fetchone()
        if row:
            return row[0], row[1]
        time.sleep(delay)
    return None, None

def pay_order(session, customer_id, order_id, amount_ignored):
    idem_key = str(uuid.uuid4())
    payload = {
        "instruction": PAYMENT_NEW_CARD  # <-- DTOTransactionRequest(instruction=...)
    }
    url = BASE_URL + PAY_ORDER.format(cid=customer_id, oid=str(order_id))
    r = session.post(url, params={"idemKey": idem_key}, json=payload, timeout=30)
    r.raise_for_status()
    return r.json() if r.headers.get("content-type","").startswith("application/json") else None

def user_flow(customer_id, product_ids):
    s = requests.Session()
    try:
        add_random_items(s, customer_id, product_ids)
        _ = checkout(s, customer_id)
        with db_conn() as conn:
            with conn.cursor() as cur:
                order_id, outstanding = fetch_latest_order_for_customer(cur, customer_id)
        if not order_id:
            return f"{customer_id[:8]}…: checkout OK, but no order found"
        _ = pay_order(s, customer_id, order_id, outstanding)
        return f"{customer_id[:8]}…: order {order_id} paid ({outstanding})"
    except requests.HTTPError as e:
        body = e.response.text[:200] if e.response is not None else ""
        code = getattr(e.response, "status_code", "???")
        return f"{customer_id[:8]}…: HTTP {code} -> {body}"
    except Exception as e:
        return f"{customer_id[:8]}…: ERROR {e}"

# =========================
# Main
# =========================
def main():
    start = time.time()
    with db_conn() as conn:
        with conn.cursor() as cur:
            product_ids = fetch_all_product_ids(cur)
            if not product_ids:
                raise RuntimeError("No products found in DB.")

    results = []
    with ThreadPoolExecutor(max_workers=CONCURRENCY) as ex:
        futs = [ex.submit(user_flow, cid, product_ids) for cid in CUSTOMERS]
        for f in as_completed(futs):
            results.append(f.result())

    for line in results:
        print(line)
    print(f"Done in {time.time()-start:.2f}s")

if __name__ == "__main__":
    main()
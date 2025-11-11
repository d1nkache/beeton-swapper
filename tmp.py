import base64
from pytoniq_core import begin_cell, Address, Slice

from pytoniq import WalletV4R2, LiteBalancer
cell = (
    begin_cell()
        .store_uint(1, 4)
        .store_uint(0, 8)
        .store_bytes(Address("EQCi9nWtRY5rdEWkZIPOe_9n1WXog8ObXCIf6RGmwFCnrrT8").hash_part)
    .end_cell()
)

boc_bytes = cell.to_boc()
boc_base64 = base64.b64encode(boc_bytes).decode()

print(boc_base64)
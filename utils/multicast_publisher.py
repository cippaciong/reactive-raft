#! /usr/bin/env python3

import argparse
import socket

parser = argparse.ArgumentParser()
parser.add_argument("message")
args = parser.parse_args()

message_bytes = args.message.encode()
multicast_group = ('230.0.0.0', 4446)
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sent = sock.sendto(message_bytes, multicast_group)
print("done sending: {}".format(args.message))

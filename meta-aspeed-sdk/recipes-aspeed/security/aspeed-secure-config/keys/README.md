# Generating Keys for U-Boot Verified Boot

This guide provides instructions for generating key pairs used in U-Boot FIT image signature verification.

## Reference
- [U-Boot FIT Signature Verification Documentation](https://docs.u-boot.org/en/latest/usage/fit/signature.html)

## Generating an RSA Key Pair and Certificate

To create a new public/private key pair, size **2048** bits:

```
$ openssl genpkey -algorithm RSA -out keys/dev.key \
    -pkeyopt rsa_keygen_bits:2048 -pkeyopt rsa_keygen_pubexp:65537
```

To create a certificate for this containing the public key:

```
$ openssl req -batch -new -x509 -key keys/dev.key -out keys/dev.crt
```

If you like you can look at the public key also:

```
$ openssl rsa -in keys/dev.key -pubout
```

## Generating an ECDSA Key Pair

To generate a new ECDSA key pair using the **secp384r1** curve:

```
$ openssl ecparam -name secp384r1 -genkey -noout -out keys/dev.pem
```

To extract the corresponding public key in PEM format:

```
$ openssl ec -in keys/dev.pem -pubout -out keys/dev-pub.pem
```

This creates keys/dev-pub.pem, which contains the public key in standard PEM
format and can be used for signature verification
(e.g., embedded in a FIT image or used on target).

If you need the public key in raw (x, y) coordinate form for device tree (DTB)
integration or debugging, you can print it with:

```
$ openssl ec -in keys/dev.pem -pubout -text -noout
```

This will display the public key as an uncompressed point in hexadecimal format
`(04 || x || y)`, which is useful for FDT properties like **ecdsa,x-point** and
**ecdsa,y-point**.


# No-buy-detector Processor

Build the Function:

```
riff function create no-buy \
  --git-repo https://github.com/projectriff-demo/no-buy-detector \
  --tail
```

Create the Streams:

```
riff streaming stream create cart-events \
    --provider franz-kafka-provisioner \
    --content-type application/json

riff streaming stream create checkout-events \
    --provider franz-kafka-provisioner \
    --content-type application/json

riff streaming stream create no-buy \
    --provider franz-kafka-provisioner \
    --content-type application/json
```

Create the Processor:

```
riff streaming processor create no-buy \
    --function-ref no-buy \
    --input cart-events \
    --input checkout-events \
    --output no-buy \
    --tail
```

curl --location 'http://localhost:8083/connectors' \
  --header 'Content-Type: application/json' \
  --data '{
    "name": "source_irc",
    "config": {
    "connector.class": "com.github.cjmatta.kafka.connect.irc.IrcSourceConnector",
    "irc.server": "irc.dal.net",
    "kafka.topic": "irc-messages",
    "irc.channels": "#Programming",
    "tasks.max": "2",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
    }
}'

# Common actors pack

## Mailing actor

Actor for sending emailsion in current chain

### Configuration example

In `"objects"` section:

```JavaScript
{
      "kind": "actor",
      "dependency": "info.smart_tools.smartactors.email.email_actor.MailingActor",
      "name": "sendMailActor",
      //name for auth on SMTP server
      "username": "example@gmail.com",
      //password for auth on SMTP server
      "password": "password",
      //authentication mode ("Login", "Plain", "None")
      "authenticationMode": "Login",
      //type of cryptographic protocol ("SSL", "TLS", "TLSv1" etc)
      "sslProtocol": "SSL",
      //mailing server's URI 
      "server": "smtps://smtp.gmail.com:465",
      //postal address from which to send letters
      "senderAddress": "examplename@gmail.com"
    }
```

In chain step:
```JavaScript
{
  "target": "sendMailActor",
  "handler": "sendMailHandler",
  "wrapper": {
    //list of recipients, contains fields recipient и type("То", "Cc", "Bcc").
    "in_getSendToMessage": "message/recipients",
    //list of attributes of email
    "in_getMessageAttributesMessage": "message/messageAttributes",
    //list of email parts
    "in_getMessagePartsMessage": "message/messageParts"
  }
}
```
### Email attributes
The following attributes are supported:

`sign` – name of sender (“this” <abrakadabra@hwdtech.com>) 

`subject` – subject of email

Example:

```JavaScript
"attributes": {
   "sign": "К.О.",
   "subject": "Example subject"
}
```

### Email parts
List of email parts is a JSON-array, elements of which are objects - descriptions of parts of the letter.

Supported values of `type` field:

`text` – Text of email. Field `text` contains string – text of letter, field `mime` –  MIME type.
`file` – attached file from fs. Field `sourceFile` contains path to  file, field `attachmentName` – the name of file that will be indicated in the letter.
`bytes-array` - bytes array. Field `source` contains bytesArray, `mime` - type of file.

Example:
```JavaScript
"parts": [
    {
        "type": "text",
        "mime": "text/plain",
        "text": "Вот тебе ещё фотографии котиков"
    },
    {
        "type": "file",
        "sourcePath": "C:\\progs\\virus.exe",
        "attachmentName": "kotiki.jpg.exe"
    },
    {
        "type": "bytes-array",
        "mime": "application/zip",
        "source": [],
        "attachmentName": "kotiki.jpg.exe"
    }
]
```



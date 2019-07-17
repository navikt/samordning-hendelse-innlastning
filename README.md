# samordning-hendelse-innlastning
Henter hendelser fra aapen-samordning-samordningspliktigHendelse-v1 kafka topic, og persisterer de i en database. 

Hendelsene eksponeres via appen [samordning-hendelse-api](https://github.com/navikt/samordning-hendelse-api). Se tilhørende README for mer informasjon.

#### Metrikker
Grafana dashboards brukes for å f.eks. monitorere minne, cpu-bruk og andre metrikker.
Se [samordning-hendelse-innlastning grafana dasboard](https://grafana.adeo.no/d/k0h45tQmz/samordning-hendelse-innlastning?orgId=1)

#### Logging
[Kibana](https://logs.adeo.no/app/kibana) benyttes til logging. Søk på f.eks. ```application:samordning-hendelse-innlastning AND envclass:q``` for logginnslag fra preprod.

#### Bygging
Jenkins benyttes til bygging. Status på bygg finner du her: [samordning-hendelse-innlastning jenkins](https://jenkins-peon.adeo.no/job/samordning-hendelse-innlastning/)

Kontakt Team Peon dersom du har noen spørsmål. Vi finnes blant annet på Slack, i kanalen [#peon](https://nav-it.slack.com/messages/C6M80587R/)

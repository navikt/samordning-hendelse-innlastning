# samordning-hendelse-innlasting
Henter hendelser fra aapen-samordning-samordningspliktigHendelse-v2 kafka topic, og persisterer de i en database. 

Hendelsene eksponeres via appen [samordning-hendelse-api](https://github.com/navikt/samordning-hendelse-api). Se tilhørende README for mer informasjon.

#### Metrikker
Grafana dashboards brukes for å f.eks. monitorere minne, cpu-bruk og andre metrikker.
Se [samordning-hendelse-innlasting grafana dasboard](https://grafana.adeo.no/d/k0h45tQmz/samordning-hendelse-innlasting?orgId=1)

#### Logging
[Kibana](https://logs.adeo.no/app/kibana) benyttes til logging. Søk på f.eks. ```application:samordning-hendelse-innlasting AND envclass:q``` for logginnslag fra preprod.

Kontakt Team Samhandling dersom du har noen spørsmål. Vi finnes blant annet på Slack, i kanalen [#samhandling_pensjonsområdet](https://nav-it.slack.com/archives/CQ08JC3UG)

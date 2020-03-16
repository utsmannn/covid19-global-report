# Rest Api Documentation Covid-19 Global Report

## Base Url
Api available at ```https://covid-19-report.herokuapp.com```
## Last update
```GET /api/last_date```
#### Response
```json
{
  "message": "OK",
  "last_date_string": "3/15/20",
  "last_date": {
    "day": 15,
    "month": 3,
    "year": 20
  }
}
```
## Status and Country
### Worldwide Status
```GET /api```
#### Parameter
| Param | Type | Desc |
|---|---|---|
| `day` | Int (required) | Day in month |
| `month` | Int (required) | Month in year |
| `year` | Int (required) | year |

#### Response
```json
{
  "message": "OK",
  "total": {
    "confirmed": 128343,
    "death": 4720,
    "recovered": 68324
  },
  "data": [
    {
      "id": 1,
      "country": "China",
      "province_or_state": "Hubei",
      "confirmed": 67781,
      "death": 3056,
      "recovered": 50318,
      "lastUpdate": 1586651325005,
      "coordinate": [
        30.9756,
        112.2707
      ]
    },
    {
      "id": 2,
      "country": "Italy",
      "province_or_state": "Unknown",
      "confirmed": 12462,
      "death": 827,
      "recovered": 1045,
      "lastUpdate": 1586564925005,
      "coordinate": [
        43.0,
        12.0
      ]
    }
  ]
}
```

### Country Status

```GET /api/country```
#### Parameter
| Param | Type | Desc |
|---|---|---|
| `day` | Int (required) | Day in month |
| `month` | Int (required) | Month in year |
| `year` | Int (required) | year |
| `q` | String (optional) | country by query |

#### Response
```json
{
  "message": "OK",
  "total": {
    "confirmed": 34,
    "death": 1,
    "recovered": 2
  },
  "countries": [
    {
      "country": "Indonesia",
      "total": {
        "confirmed": 34,
        "death": 1,
        "recovered": 2
      },
      "data": [
        {
          "id": 84,
          "country": "Indonesia",
          "province_or_state": "Unknown",
          "confirmed": 34,
          "death": 1,
          "recovered": 2,
          "lastUpdate": 1586565501219,
          "coordinate": [
            -0.7893,
            113.9213
          ]
        }
      ]
    }
  ]
}
```

## Timeline and Situation Document
### Timeline (only available in march)
```GET /api/stat```

#### Parameter
| Param | Type | Desc |
|---|---|---|
| `q` | String (optional) | country by query |

#### Response
```json
{
  "message": "OK",
  "timeLine": {
    "country": "indonesia",
    "timeLine": [
      {
        "date": "4/3/20",
        "total": {
          "confirmed": 2,
          "death": 0,
          "recovered": 0
        }
      },
      {
        "date": "6/3/20",
        "total": {
          "confirmed": 4,
          "death": 0,
          "recovered": 0
        }
      }
    ]
  }
}
```

### WHO Situation Report
```GET /api/sit_rep```
#### Response
```json
{
  "download_url": "https://github.com/CSSEGISandData/COVID-19/raw/master/who_covid_19_situation_reports/who_covid_19_sit_rep_pdfs/20200310-sitrep-50-covid-19.pdf"
}
```

### Articles
```GET /api/articles```
#### Parameter
| Param | Type | Desc |
|---|---|---|
| `q` | String (optional) | country by query |

#### Response
```json
{
  "message": "OK",
  "topic": "indonesia",
  "articles": [
    {
      "title": "Indonesia's Covid-19 Cases Rise to 117 as Cabinet Members Take Test for Coronavirus - Jakarta Globe",
      "url": "https://jakartaglobe.id/news/indonesias-covid19-cases-rise-to-117-as-cabinet-members-take-test-for-coronavirus",
      "publish_date": 1584279133000,
      "publisher": "Jakarta Globe"
    },
    {
      "title": "COVID-19: Indonesia should get grip on reality and work with Singapore - The Jakarta Post - Jakarta Post",
      "url": "https://www.thejakartapost.com/academia/2020/03/15/covid-19-indonesia-should-get-grip-on-reality-and-work-with-singapore.html",
      "publish_date": 1584245100000,
      "publisher": "Jakarta Post"
    },
    {
      "title": "Covid-19: Indonesia cases surge to 117 - New Straits Times",
      "url": "https://www.nst.com.my/world/world/2020/03/574834/covid-19-indonesia-cases-surge-117",
      "publish_date": 1584261696000,
      "publisher": "New Straits Times"
    }
  ]
}
```

---
Data source from : https://github.com/CSSEGISandData/COVID-19 




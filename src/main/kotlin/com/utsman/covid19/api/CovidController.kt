package com.utsman.covid19.api

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.utsman.covid19.api.model.*
import org.json.JSONException
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.lang.IllegalStateException
import java.net.SocketException
import java.net.URL
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.*
import javax.net.ssl.SSLHandshakeException


@RestController
@RequestMapping("/")
class CovidController {
    private val restTemplate = RestTemplate()
    private val author = "Restful API by Muhammad Utsman, data provided by Johns Hopkins University Center for Systems Science and Engineering (JHU CSSE)"

    private val sources = listOf(
            Sources("World Health Organization (WHO)", "https://www.who.int/"),
            Sources("DXY.cn. Pneumonia. 2020", "http://3g.dxy.cn/newh5/view/pneumonia"),
            Sources("BNO News", "https://bnonews.com/index.php/2020/02/the-latest-coronavirus-cases/"),
            Sources("National Health Commission of the People’s Republic of China (NHC)", "http://www.nhc.gov.cn/xcs/yqtb/list_gzbd.shtml"),
            Sources("China CDC (CCDC)", "http://weekly.chinacdc.cn/news/TrackingtheEpidemic.htm"),
            Sources("Hong Kong Department of Health", "https://www.chp.gov.hk/en/features/102465.html"),
            Sources("Macau Government", "https://www.ssm.gov.mo/portal/"),
            Sources("Taiwan CDC", "https://sites.google.com/cdc.gov.tw/2019ncov/taiwan?authuser=0"),
            Sources("US CDC", "https://www.cdc.gov/coronavirus/2019-ncov/index.html"),
            Sources("Government of Canada", "https://www.canada.ca/en/public-health/services/diseases/coronavirus.html"),
            Sources("Australia Government Department of Health", "https://www.health.gov.au/news/coronavirus-update-at-a-glance"),
            Sources("European Centre for Disease Prevention and Control (ECDC)", "https://www.ecdc.europa.eu/en/geographical-distribution-2019-ncov-cases"),
            Sources("Ministry of Health Singapore (MOH)", "https://www.moh.gov.sg/covid-19"),
            Sources("Italy Ministry of Health", "http://www.salute.gov.it/nuovocoronavirus")
    )

    @GetMapping("/api")
    fun getAll(@RequestParam("day") day: Int,
               @RequestParam("month") month: Int,
               @RequestParam("year") year: Int,
               @RequestParam("q") country: String?): Responses? {

        var message = "OK"

        val formatter = DecimalFormat("00")
        val dayFormat = formatter.format(day.toLong())
        val monthFormat = formatter.format(month.toLong())

        val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/$monthFormat-$dayFormat-$year.csv"

        val listData: MutableList<Data> = mutableListOf()
        val finalListData: MutableList<Data> = mutableListOf()

        try {
            val responsesString = restTemplate.getForObject(url, String::class.java)
            val obj = responsesString?.let { csvReader().readAll(it) }
            println(obj)

            obj?.forEachIndexed { index, list ->
                if (index != 0 && index != obj.size) {
                    try {
                        val lastUpdate = LocalDateTime
                                .parse(list.get(2))
                                .toLocalDate()

                        val cal = Calendar.getInstance().apply {
                            set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                        }
                        val time = cal.time.time

                        println(lastUpdate)
                        val data = Data(
                                id = index,
                                country = list.get(1),
                                province_or_state = if (list.get(0) == "") "Unknown" else list.get(0),
                                confirmed = list.get(3).toInt(),
                                death = list.get(4).toInt(),
                                recovered = list.get(5).toInt(),
                                lastUpdate = time,
                                coordinate = listOf(list.get(6).toDouble(), list.get(7).toDouble()))
                        listData.add(data)

                    } catch (e: IndexOutOfBoundsException) {
                        message = "Data not yet available"
                    } catch (e: DateTimeParseException) {
                        message = "Data not yet available"
                    }
                }
            }
        } catch (e: HttpClientErrorException) {
            message = "Data not yet available"
        } catch (e: SocketException) {
            message = "Data not yet available"
        } catch (e: SSLHandshakeException) {
            message = "Data not yet available"
        }

        if (country != null) {
            val newFilterData = listData.filter { it.country?.toLowerCase()?.contains(country.toLowerCase()) == true }
            finalListData.addAll(newFilterData)
        } else {
            finalListData.addAll(listData)
        }

        val totalConfirmed = listData.sumBy { it.confirmed ?: 0 }
        val totalDeath = listData.sumBy { it.death ?: 0 }
        val totalRecovered = listData.sumBy { it.recovered ?: 0 }

        return Responses(
                message = message,
                total = Total(
                        confirmed = totalConfirmed,
                        death = totalDeath,
                        recovered = totalRecovered
                ),
                data = finalListData,
                sources = sources,
                author = author
        )
    }

    @GetMapping("/api/country")
    fun getByCountry(@RequestParam("day") day: Int,
                     @RequestParam("month") month: Int,
                     @RequestParam("year") year: Int,
                     @RequestParam("q") country: String?): ResponsesCountry {

        var message = "OK"

        val formatter = DecimalFormat("00")
        val dayFormat = formatter.format(day.toLong())
        val monthFormat = formatter.format(month.toLong())

        val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/$monthFormat-$dayFormat-$year.csv"

        val listDataCountries: MutableList<DataCountry> = mutableListOf()
        val listData: MutableList<Data> = mutableListOf()
        val finalListData: MutableList<DataCountry> = mutableListOf()

        try {
            val responsesString = restTemplate.getForObject(url, String::class.java)
            val obj = responsesString?.let { csvReader().readAll(it) }
            println(obj)

            obj?.forEachIndexed { index, list ->
                if (index != 0 && index != obj.size) {
                    try {
                        val lastUpdate = LocalDateTime
                                .parse(list.get(2))
                                .toLocalDate()

                        val cal = Calendar.getInstance().apply {
                            set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                        }
                        val time = cal.time.time

                        println(lastUpdate)
                        val data = Data(
                                id = index,
                                country = list.get(1),
                                province_or_state = if (list.get(0) == "") "Unknown" else list.get(0),
                                confirmed = list.get(3).toInt(),
                                death = list.get(4).toInt(),
                                recovered = list.get(5).toInt(),
                                lastUpdate = time,
                                coordinate = listOf(list.get(6).toDouble(), list.get(7).toDouble()))
                        listData.add(data)

                    } catch (e: IndexOutOfBoundsException) {
                        message = "Data not yet available"
                    } catch (e: DateTimeParseException) {
                        message = "Data not yet available"
                    }
                }
            }

            val listCountry = listData.groupBy { it.country }
            listDataCountries.addAll(
                    listCountry.map {
                        DataCountry(
                                country = it.key,
                                total = Total(
                                        it.value.sumBy { d -> d.confirmed ?: 0 },
                                        it.value.sumBy { d -> d.death ?: 0 },
                                        it.value.sumBy { d -> d.recovered ?: 0 }),
                                data = it.value)
                    }
            )

        } catch (e: HttpClientErrorException) {
            message = "Data not yet available"
        } catch (e: SocketException) {
            message = "Data not yet available"
        } catch (e: SSLHandshakeException) {
            message = "Data not yet available"
        }

        if (country != null) {
            val newFilterData = listDataCountries.filter { it.country?.toLowerCase()?.contains(country.toLowerCase()) == true }
            finalListData.addAll(newFilterData)
        } else {
            finalListData.addAll(listDataCountries)
        }

        val totalConfirmed = finalListData.sumBy { it.total.confirmed }
        val totalDeath = finalListData.sumBy { it.total.death }
        val totalRecovered = finalListData.sumBy { it.total.recovered }

        return ResponsesCountry(
                message = message,
                total = Total(
                        confirmed = totalConfirmed,
                        death = totalDeath,
                        recovered = totalRecovered
                ),
                countries = finalListData,
                sources = sources,
                author = author
        )
    }

    @GetMapping("api/stat")
    fun getTimeline(@RequestParam("q") country: String?): ResponsesTimeLine {
        val date1 = "4-3-2020"
        val date2 = "6-3-2020"
        val date3 = "8-3-2020"
        val date4 = "10-3-2020"
        val date5 = "12-3-2020"

        val day1 = getByCountry(4, 3, 2020, country).total
        val day2 = getByCountry(6, 3, 2020, country).total
        val day3 = getByCountry(8, 3, 2020, country).total
        val day4 = getByCountry(10, 3, 2020, country).total
        val day5 = getByCountry(12, 3, 2020, country).total

        val dataTimeLine1 = DataTimeLine(date1, day1)
        val dataTimeLine2 = DataTimeLine(date2, day2)
        val dataTimeLine3 = DataTimeLine(date3, day3)
        val dataTimeLine4 = DataTimeLine(date4, day4)
        val dataTimeLine5 = DataTimeLine(date5, day5)
        val timeline = TimeLine(country
                ?: "", listOf(dataTimeLine1, dataTimeLine2, dataTimeLine3, dataTimeLine4, dataTimeLine5))
        return ResponsesTimeLine(
                message = "OK",
                timeLine = timeline,
                sources = sources,
                author = author
        )
    }

    @GetMapping("/api/last_date")
    fun getLastDate(): ResponsesLastDate {
        val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv"
        var message = "OK"
        var dateString: String? = ""

        try {
            val responsesString = restTemplate.getForObject(url, String::class.java)
            val obj = responsesString?.let { csvReader().readAll(it) }
            val listDate = obj?.get(0)
            val listValue = obj?.get(1)

            dateString = if (listValue?.last() != "") {
                listDate?.last()
            } else {
                listDate?.get(listDate.size-2)
            }

        } catch (e: HttpClientErrorException) {
            message = "Data not yet available"
        } catch (e: SocketException) {
            message = "Data not yet available"
        } catch (e: SSLHandshakeException) {
            message = "Data not yet available"
        }

        val dateList = dateString?.split("/")
        val day = dateList?.get(1)?.toIntOrNull()
        val month = dateList?.get(0)?.toIntOrNull()
        val year = dateList?.get(2)?.toIntOrNull()
        val lastDate = LastDate(day, month, year)

        return ResponsesLastDate(message, dateString, lastDate)
    }

    @GetMapping("/api/articles", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getArticles(@RequestParam("q") q: String): ResponsesArticles? {
        val articles: MutableList<Articles> = mutableListOf()
        var message = "OK"
        val urlXml = "https://news.google.com/rss/search?q=covid%20$q&hl=en-ID&gl=ID&ceid=ID:en"

        try {

            val synd = SyndFeedInput().build(XmlReader(URL(urlXml)))
            println(synd.title)

            val art = synd.entries.map { ent ->
                println(ent.publishedDate)
                println(ent.source.title)
                Articles(
                        title = ent.title,
                        url = ent.link,
                        publish_date = ent.publishedDate.time,
                        publisher = ent.source.title
                )
            }

            articles.addAll(art)

        } catch (e: JSONException) {
            e.printStackTrace()
            message = "Failed"
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            message = "Failed"
        } catch (e: HttpStatusException) {
            e.printStackTrace()
            message = "Failed"
        } catch (e: SocketException) {
            e.printStackTrace()
            message = "Failed"
        }

        return ResponsesArticles(
                message = message,
                topic = q,
                articles = articles,
                author = author
        )
    }

    @GetMapping("/image_thumbnail")
    fun getThumbnail(@RequestParam("url") url: String): ResponsesImage? {
        val doc = Jsoup.connect(url).get()
        val element = doc.select("meta")

        val imageUrlElement = element.map { it.attr("content")  }
        val imageUrl = imageUrlElement.find { it.toLowerCase().contains(".jpg") || it.toLowerCase().contains(".png") }

        println(imageUrl)

        return ResponsesImage(imageUrl)
    }
}

fun String.getNumber(): Int {
    return this.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
}
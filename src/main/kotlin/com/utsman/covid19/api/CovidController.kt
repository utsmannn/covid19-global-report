package com.utsman.covid19.api

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.utsman.covid19.api.model.*
import com.utsman.covid19.api.raw_model.RawMasterRecursive
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

    @GetMapping("/api", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getData(@RequestParam("q") country: String?): Responses? {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DATE)
        val month = calendar.get(Calendar.MONTH)+1
        val year = calendar.get(Calendar.YEAR)
        return getAll(day, month, year, country)
    }

    private fun url(day: Int, month: Int, year: Int): String {
        val formatter = DecimalFormat("00")
        val dayFormat = formatter.format(day.toLong())
        val monthFormat = formatter.format(month.toLong())
        return "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/$monthFormat-$dayFormat-$year.csv"
    }

    private fun getResponsesString(day: Int, month: Int, year: Int, result: (responses: String, dayFound: Int, monthFound: Int) -> Unit) {
        try {
            println("try get $day")
            val rest = restTemplate.getForObject(url(day, month, year), String::class.java)
            if (rest != null) result.invoke(rest, day, month)
            else getResponsesString(day-1, month, year) { responses, dayFound, monthFound ->
                getResponsesString(23, month, year, result)
            }
        } catch (e: HttpClientErrorException) {
            println("try get -1 $day")
            getResponsesString(day-1, month, year) { responses, dayFound, monthFound ->
                result.invoke(responses, dayFound, monthFound)
            }
        } catch (e: SocketException) {
        } catch (e: SSLHandshakeException) {
        }

    }

    private fun getAll(day: Int, month: Int, year: Int, country: String?): Responses? {
        var message = "OK"
        var lastUpdateGlobal = ""

        val listData: MutableList<Data> = mutableListOf()
        val finalListData: MutableList<Data> = mutableListOf()

        getResponsesString(day, month, year) { responsesString, dayFound, monthFound ->
            println("success get responses")
            lastUpdateGlobal = "$day/$monthFound/2020"
            val obj = csvReader().readAll(responsesString)
            obj.forEachIndexed { index, list ->
                if (index != 0 && index != obj.size) {

                    if (dayFound < 23) {
                        try {
                            val lastUpdate = LocalDateTime
                                    .parse(list[2])
                                    .toLocalDate()

                            val cal = Calendar.getInstance().apply {
                                set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                            }
                            val time = cal.time.time
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
                            message = "Failed"
                        } catch (e: DateTimeParseException) {
                            message = "Failed"
                        }
                    } else {
                        try {
                            val lastUpdate = LocalDateTime
                                    .parse(list[4].replace(" ","T"))
                                    .toLocalDate()

                            val cal = Calendar.getInstance().apply {
                                set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                            }
                            val time = cal.time.time
                            val data = Data(
                                    id = index,
                                    country = list.get(3),
                                    province_or_state = if (list.get(2) == "") "Unknown" else list.get(2),
                                    confirmed = list.get(7).toInt(),
                                    death = list.get(8).toInt(),
                                    recovered = list.get(9).toInt(),
                                    lastUpdate = time,
                                    coordinate = listOf(list.get(5).toDouble(), list.get(6).toDouble()))
                            listData.add(data)

                        } catch (e: IndexOutOfBoundsException) {
                            message = "Failed"
                        } catch (e: DateTimeParseException) {
                            message = "Failed"
                        }
                    }
                }
            }
        }

        if (country != null) {
            val newFilterData = listData.filter { it.country?.toLowerCase()?.contains(country.toLowerCase()) == true }
            finalListData.addAll(newFilterData)
        } else {
            finalListData.addAll(listData)
        }

        val totalConfirmed = finalListData.sumBy { it.confirmed ?: 0 }
        val totalDeath = finalListData.sumBy { it.death ?: 0 }
        val totalRecovered = finalListData.sumBy { it.recovered ?: 0 }

        return Responses(
                message = message,
                total = Total(
                        confirmed = totalConfirmed,
                        death = totalDeath,
                        recovered = totalRecovered
                ),
                data = finalListData,
                author = author,
                last_update = lastUpdateGlobal
        )
    }

    @GetMapping("api/stat", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTimeline(@RequestParam("q") country: String?): ResponsesTimeLine {

        val responsesCountry = getData(country)
        val lastUpdate = responsesCountry?.last_update?.split("/") ?: emptyList()

        val date1 = "${lastUpdate[0].toInt()/5}/3/20"
        val date2 = "${lastUpdate[0].toInt()/4}/3/20"
        val date3 = "${lastUpdate[0].toInt()/3}/3/20"
        val date4 = "${lastUpdate[0].toInt()/2}/3/20"
        val dateNow = "${lastUpdate[0].toInt()}/3/20"

        val totalNow = getAll(lastUpdate[0].toInt(), lastUpdate[1].toInt(), 2020, country)?.total
        val total1 = getAll(lastUpdate[0].toInt()/5, lastUpdate[1].toInt(), 2020, country)?.total
        val total2 = getAll(lastUpdate[0].toInt()/4, lastUpdate[1].toInt(), 2020, country)?.total
        val total3 = getAll(lastUpdate[0].toInt()/3, lastUpdate[1].toInt(), 2020, country)?.total
        val total4 = getAll(lastUpdate[0].toInt()/2, lastUpdate[1].toInt(), 2020, country)?.total


        val dataTimeLine1 = DataTimeLine(date1, total1)
        val dataTimeLine2 = DataTimeLine(date2, total2)
        val dataTimeLine3 = DataTimeLine(date3, total3)
        val dataTimeLine4 = DataTimeLine(date4, total4)
        val dataTimeLine5 = DataTimeLine(dateNow, totalNow)
        val timeline = TimeLine(country
                ?: "", listOf(dataTimeLine1, dataTimeLine2, dataTimeLine3, dataTimeLine4, dataTimeLine5))
        return ResponsesTimeLine(
                message = "OK",
                timeLine = timeline,
                author = author
        )
    }

    @GetMapping("/api/last_date", produces = [MediaType.APPLICATION_JSON_VALUE])
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
                listDate?.get(listDate.size - 2)
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

    @GetMapping("/api/image_thumbnail", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getThumbnail(@RequestParam("url") url: String): ResponsesImage? {
        val doc = Jsoup.connect(url).get()
        val element = doc.select("meta")

        val imageUrlElement = element.map { it.attr("content") }
        val imageUrl = imageUrlElement.find { it.toLowerCase().contains(".jpg") || it.toLowerCase().contains(".png") }

        println(imageUrl)

        return ResponsesImage(imageUrl)
    }

    @GetMapping("/api/sit_rep", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSitRep(): ResponseSituationReport {
        var pathPdf = ""
        val urlMaster = "https://api.github.com/repos/CSSEGISandData/COVID-19/git/trees/master?recursive=1"
        val responsesMaster = restTemplate.getForObject(urlMaster, RawMasterRecursive::class.java)
        val findPdfCommitPath = responsesMaster?.tree?.find { it?.path?.contains("sit_rep_pdfs") == true }?.url

        pathPdf = if (findPdfCommitPath != null) {
            val responsePdfSitRep = restTemplate.getForObject(findPdfCommitPath, RawMasterRecursive::class.java)
            val pathName = responsePdfSitRep?.tree?.last()?.path
            val pathDownload = "https://github.com/CSSEGISandData/COVID-19/raw/master/who_covid_19_situation_reports/who_covid_19_sit_rep_pdfs/$pathName"
            pathDownload
        } else {
            ""
        }

        return ResponseSituationReport(pathPdf)
    }
}
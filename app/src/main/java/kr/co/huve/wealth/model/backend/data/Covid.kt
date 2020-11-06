package kr.co.huve.wealth.model.backend.data

import java.io.Serializable
import javax.xml.bind.annotation.*

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
data class CovidResponse(
    @XmlElement(name = "header")
    val header: Header = Header(),
    @XmlElement(name = "body")
    val body: Body = Body()
) : Serializable

@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
data class Header(
    @XmlElement(name = "resultCode")
    val resultCode: String = "",
    @XmlElement(name = "resultMsg")
    val resultMsg: String = ""
) : Serializable


@XmlRootElement(name = "body")
@XmlAccessorType(XmlAccessType.FIELD)
data class Body(

    @field:XmlElementWrapper(name = "items")
    @field:XmlElement(name = "item")
    val items: List<Data>? = arrayListOf()
) : Serializable

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
data class Data(
    // 데이터 생성 날짜
    @field:XmlElement(name = "createDt")
    val createDateString: String = "",
    // 해당 지역 사망자 수
    @field:XmlElement(name = "deathCnt")
    val deathCount: Int = 0,
    // 해당 지역 확진자 수
    @field:XmlElement(name = "defCnt")
    val covidCount: Int = 0,
    // 해당 지역명
    @field:XmlElement(name = "gubun")
    val region: String = "",
    // 전일 대비 증감 수
    @field:XmlElement(name = "incDec")
    val increasedCount: Int = 0,
    // 격리 해제 수
    @field:XmlElement(name = "isolClearCnt")
    val isolationDoneCount: Int = 0,
    // 격리 수
    @field:XmlElement(name = "isolIngCnt")
    val isolatingCount: Int = 0,
    // 지역 발생 수
    @field:XmlElement(name = "localOccCnt")
    val localOccurCount: Int = 0,
    // 유입 발생
    @field:XmlElement(name = "overFlowCnt")
    val inflowCount: Int = 0,
    // 10만명 당 발생 비율
    @field:XmlElement(name = "qurRate")
    val occurrencePerTenThousand: Int = 0
) : Serializable
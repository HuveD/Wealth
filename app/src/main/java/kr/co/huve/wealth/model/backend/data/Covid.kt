package kr.co.huve.wealth.model.backend.data

import java.io.Serializable

//@XmlRootElement(name = "response")
data class CovidResponse(
//    @XmlElement(name = "header")
    val header: Header,
//    @XmlElement(name = "body")
    val body: Body
) : Serializable

//@XmlRootElement(name = "header")
data class Header(
//    @XmlElement(name = "resultCode")
    val resultCode: String,
//    @XmlElement(name = "resultMsg")
    val resultMsg: String
) : Serializable


//@XmlRootElement(name = "body")
data class Body(

//    @field:XmlElementWrapper(name = "items")
//    @field:XmlElement(name = "item")
    val items: List<Data>?
) : Serializable

//@XmlRootElement(name = "item")
data class Data(
    // 데이터 생성 날짜
//    @field:XmlElement(name = "createDt")
    val createDateString: String,
    // 해당 지역 사망자 수
//    @field:XmlElement(name = "deathCnt")
    val deathCount: Int,
    // 해당 지역 확진자 수
//    @field:XmlElement(name = "defCnt")
    val covidCount: Int,
    // 해당 지역명
//    @field:XmlElement(name = "gubun")
    val region: String,
    // 전일 대비 증감 수
//    @field:XmlElement(name = "incDec")
    val increasedCount: Int,
    // 격리 해제 수
//    @field:XmlElement(name = "isolClearCnt")
    val isolationDoneCount: Int,
    // 격리 수
//    @field:XmlElement(name = "isolIngCnt")
    val isolatingCount: Int,
    // 지역 발생 수
//    @field:XmlElement(name = "localOccCnt")
    val localOccurCount: Int,
    // 유입 발생
//    @field:XmlElement(name = "overFlowCnt")
    val inflowCount: Int,
    // 10만명 당 발생 비율
//    @field:XmlElement(name = "qurRate")
    val occurrencePerTenThousand: Int,
) : Serializable
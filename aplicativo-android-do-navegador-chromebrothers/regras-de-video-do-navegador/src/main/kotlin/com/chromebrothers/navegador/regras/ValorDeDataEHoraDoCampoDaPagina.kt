package com.chromebrothers.navegador.regras

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

/** Converte o valor dos campos de data e hora do HTML (date, month, week, time, datetime-local). */
object ValorDeDataEHoraDoCampoDaPagina {
    fun data(valor: String?): LocalDate? {
        val texto = valor?.trim().orEmpty()
        val semana = Regex("""(\d{4,})-W(\d{2})""").matchEntire(texto)
        return runCatching {
            when {
                semana != null -> segundaDaSemanaIso(semana.groupValues[1].toInt(), semana.groupValues[2].toInt())
                texto.matches(Regex("""\d{4,}-\d{2}""")) -> YearMonth.parse(texto).atDay(1)
                else -> LocalDate.parse(texto.substringBefore('T').substringBefore(' '))
            }
        }.getOrNull()
    }

    fun hora(valor: String?): LocalTime? {
        val texto = valor?.trim().orEmpty().substringAfter('T').substringAfter(' ')
        return runCatching { LocalTime.parse(texto) }.getOrNull()
    }

    fun textoDaData(data: LocalDate): String = data.toString()

    fun textoDoMes(data: LocalDate): String = YearMonth.from(data).toString()

    fun textoDaSemana(data: LocalDate): String {
        val ano = data.get(IsoFields.WEEK_BASED_YEAR).toString().padStart(4, '0')
        val semana = data.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString().padStart(2, '0')
        return "$ano-W$semana"
    }

    fun textoDaHora(hora: LocalTime): String = hora.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun textoDaDataEHora(data: LocalDate, hora: LocalTime): String =
        LocalDateTime.of(data, hora).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

    private fun segundaDaSemanaIso(ano: Int, semana: Int): LocalDate {
        // 4 de janeiro está sempre na semana 1.
        val dia = LocalDate.of(ano, 1, 4).with(DayOfWeek.MONDAY).plusWeeks((semana - 1).toLong())
        require(semana >= 1 && dia.get(IsoFields.WEEK_BASED_YEAR) == ano)
        return dia
    }
}

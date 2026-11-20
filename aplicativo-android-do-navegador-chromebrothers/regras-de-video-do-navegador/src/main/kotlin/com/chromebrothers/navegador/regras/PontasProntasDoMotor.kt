package com.chromebrothers.navegador.regras

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** Uma ponta (forma do carimbo) que vem com o app. */
data class PontaPronta(val id: String, val nome: String) {
    /** Arquivo da máscara na pasta das formas (versão no nome: mudar o desenho cria arquivo novo). */
    val arquivo: String get() = "ponta-pronta-$id-v1.png"
}

/**
 * Pontas desenhadas pelo próprio app (sem imagens de terceiros): cada uma é gerada por ruído com semente
 * fixa, sempre igual. A saída é a opacidade (0..255) de cada pixel de uma máscara quadrada — o claro vira
 * tinta, como as formas importadas.
 */
object PontasProntasDoMotor {
    const val LADO = 256

    val LAPIS = PontaPronta("lapis", "Lápis grafite")
    val CARVAO = PontaPronta("carvao", "Carvão")
    val GIZ_PASTEL = PontaPronta("giz-pastel", "Giz pastel")
    val AQUARELA = PontaPronta("aquarela", "Aquarela")
    val CERDAS = PontaPronta("cerdas", "Cerdas")
    val PINCEL_SECO = PontaPronta("pincel-seco", "Pincel seco")
    val RESPINGOS = PontaPronta("respingos", "Respingos")
    val ESPONJA = PontaPronta("esponja", "Esponja")
    val CHANFRADO = PontaPronta("chanfrado", "Marcador chanfrado")
    val TINTEIRO = PontaPronta("tinteiro", "Pena tinteiro")
    val NEVOA = PontaPronta("nevoa", "Névoa")
    val HACHURA = PontaPronta("hachura", "Hachura")

    val TODAS: List<PontaPronta> = listOf(
        LAPIS, CARVAO, GIZ_PASTEL, AQUARELA, CERDAS, PINCEL_SECO,
        RESPINGOS, ESPONJA, CHANFRADO, TINTEIRO, NEVOA, HACHURA,
    )

    fun porArquivo(arquivo: String): PontaPronta? = TODAS.firstOrNull { it.arquivo == arquivo }

    /** Opacidade de cada pixel (linha a linha), 0..255. */
    fun alfas(ponta: PontaPronta, lado: Int = LADO): IntArray {
        val saida = IntArray(lado * lado)
        val pontos = when (ponta.id) {
            CERDAS.id -> pontosDeCerdas(semente = 11, quantidade = 34, falhas = 0f)
            PINCEL_SECO.id -> pontosDeCerdas(semente = 23, quantidade = 26, falhas = 0.45f)
            RESPINGOS.id -> pontosDeRespingo()
            else -> emptyList()
        }
        val px = 2f / lado
        for (j in 0 until lado) {
            for (i in 0 until lado) {
                val u = (i + 0.5f) * px - 1f
                val v = (j + 0.5f) * px - 1f
                val a = when (ponta.id) {
                    LAPIS.id -> lapis(u, v, px)
                    CARVAO.id -> carvao(u, v, px)
                    GIZ_PASTEL.id -> gizPastel(u, v, px)
                    AQUARELA.id -> aquarela(u, v, px)
                    CERDAS.id, PINCEL_SECO.id, RESPINGOS.id -> discos(u, v, px, pontos)
                    ESPONJA.id -> esponja(u, v, px)
                    CHANFRADO.id -> chanfrado(u, v, px)
                    TINTEIRO.id -> tinteiro(u, v, px)
                    NEVOA.id -> nevoa(u, v)
                    HACHURA.id -> hachura(u, v, px)
                    else -> 0f
                }
                saida[j * lado + i] = (a.coerceIn(0f, 1f) * 255f).roundToInt()
            }
        }
        return saida
    }

    // ---------- Formas ----------

    private fun lapis(u: Float, v: Float, px: Float): Float {
        val r = hypot(u, v)
        val borda = 0.62f + 0.08f * (ruido(atan2(v, u) * 3f, 0f, 5) - 0.5f)
        val disco = borda(r, borda, px * 3f)
        val grafite = ruido(u * 48f, v * 48f, 7)
        return disco * (0.35f + 0.65f * grafite.pow(1.6f)) * (1f - 0.35f * (r / 0.62f).coerceIn(0f, 1f))
    }

    private fun carvao(u: Float, v: Float, px: Float): Float {
        val r = hypot(u, v)
        val angulo = atan2(v, u)
        val borda = 0.78f + 0.16f * (fbm(cos(angulo) * 2.2f + 3f, sin(angulo) * 2.2f + 3f, 13) - 0.5f) * 2f
        val disco = borda(r, borda, px * 4f)
        val grao = fbm(u * 9f, v * 9f, 17)
        return disco * suave(0.3f, 0.62f, grao) * (0.75f + 0.25f * ruido(u * 60f, v * 60f, 19))
    }

    private fun gizPastel(u: Float, v: Float, px: Float): Float {
        // Superelipse (quase quadrada) com borda lascada.
        val forma = (abs(u).pow(4f) + abs(v).pow(4f)).pow(0.25f)
        val borda = 0.72f + 0.1f * (fbm(u * 5f + 7f, v * 5f + 7f, 29) - 0.5f) * 2f
        val corpo = borda(forma, borda, px * 3f)
        // Grão em coordenadas giradas (sem a "grade" do ruído) e suave: textura de pastel, não de pixel.
        val gu = u * 0.8f - v * 0.6f
        val gv = u * 0.6f + v * 0.8f
        val grao = fbm(gu * 16f, gv * 22f, 31)
        return corpo * (0.6f + 0.4f * suave(0.32f, 0.62f, grao))
    }

    private fun aquarela(u: Float, v: Float, px: Float): Float {
        // Mancha irregular, miolo ralo e borda mais escura (a tinta seca na beirada).
        val du = (fbm(u * 2f + 1f, v * 2f, 37) - 0.5f) * 0.35f
        val dv = (fbm(u * 2f, v * 2f + 5f, 41) - 0.5f) * 0.35f
        val r = hypot(u + du, v + dv)
        val raio = 0.8f
        val dentro = borda(r, raio, px * 6f)
        val beirada = suave(raio * 0.7f, raio * 0.97f, r)
        val textura = 0.85f + 0.15f * fbm(u * 8f, v * 8f, 43)
        return dentro * (0.3f + 0.6f * beirada) * textura
    }

    private fun esponja(u: Float, v: Float, px: Float): Float {
        val r = hypot(u, v)
        val disco = borda(r, 0.85f, px * 10f)
        val poros = suave(0.4f, 0.5f, fbm(u * 0.7f * 16f - v * 0.7f * 4f, u * 0.7f * 4f + v * 0.7f * 16f, 47))
        return disco * poros
    }

    /** Ponta chata de marcador (o ângulo do pincel define a inclinação). */
    private fun chanfrado(u: Float, v: Float, px: Float): Float {
        val canto = 0.04f
        val qx = abs(u) - (0.2f - canto)
        val qy = abs(v) - (0.88f - canto)
        val fora = hypot(max(qx, 0f), max(qy, 0f)) + min(max(qx, qy), 0f) - canto
        return 1f - suave(-px * 1.5f, px * 1.5f, fora)
    }

    private fun tinteiro(u: Float, v: Float, px: Float): Float {
        val e = hypot(u / 0.36f, v / 0.92f)
        return borda(e, 1f, px * 4f)
    }

    private fun nevoa(u: Float, v: Float): Float {
        val r = hypot(u, v)
        val queda = (1f - r).coerceIn(0f, 1f).pow(1.6f)
        return queda * (0.4f + 0.6f * fbm(u * 3f + 2f, v * 3f + 2f, 53))
    }

    private fun hachura(u: Float, v: Float, px: Float): Float {
        val r = hypot(u, v)
        val disco = borda(r, 0.9f, px * 3f)
        val d = (u + v) / 1.4142f
        val periodo = 0.3f
        val fase = abs(((d / periodo) - floor(d / periodo)) - 0.5f) * periodo
        val linha = 1f - suave(0.035f - px, 0.035f + px, fase)
        return disco * linha * (0.8f + 0.2f * ruido(u * 20f, v * 20f, 59))
    }

    // ---------- Discos (cerdas, respingos) ----------

    private data class Disco(val x: Float, val y: Float, val raio: Float, val forca: Float)

    /** Cerdas: pontinhos numa coluna (o carimbo gira com o traço e cada um vira um fio). */
    private fun pontosDeCerdas(semente: Int, quantidade: Int, falhas: Float): List<Disco> {
        val sorteio = Random(semente)
        return List(quantidade) {
            val y = (it + 0.5f) / quantidade * 1.8f - 0.9f + (sorteio.nextFloat() - 0.5f) * 0.04f
            Disco(
                x = (sorteio.nextFloat() - 0.5f) * 0.3f,
                y = y,
                raio = 0.025f + sorteio.nextFloat() * 0.035f,
                forca = if (sorteio.nextFloat() < falhas) 0.25f else 0.7f + sorteio.nextFloat() * 0.3f,
            )
        }
    }

    private fun pontosDeRespingo(): List<Disco> {
        val sorteio = Random(61)
        return List(46) {
            val angulo = sorteio.nextFloat() * 2f * PI.toFloat()
            val distancia = sorteio.nextFloat().pow(0.7f) * 0.85f
            Disco(
                x = cos(angulo) * distancia,
                y = sin(angulo) * distancia,
                raio = (0.015f + sorteio.nextFloat().pow(2.5f) * 0.12f) * (1f - distancia * 0.5f),
                forca = 0.75f + sorteio.nextFloat() * 0.25f,
            )
        }
    }

    private fun discos(u: Float, v: Float, px: Float, discos: List<Disco>): Float {
        var a = 0f
        discos.forEach { d ->
            val r = hypot(u - d.x, v - d.y)
            if (r < d.raio + px * 2f) a = max(a, borda(r, d.raio, px * 2f) * d.forca)
        }
        return a
    }

    // ---------- Ferramentas ----------

    /** 1 dentro do raio, 0 fora, com uma faixa de [largura] suavizada (borda sem serrilhado). */
    private fun borda(distancia: Float, raio: Float, largura: Float): Float =
        1f - suave(raio - largura, raio + largura * 0.5f, distancia)

    private fun suave(de: Float, ate: Float, x: Float): Float {
        val t = ((x - de) / (ate - de).takeIf { it != 0f }!!).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Ruído de valor (0..1), suave, com semente. */
    private fun ruido(x: Float, y: Float, semente: Int): Float {
        val x0 = floor(x).toInt()
        val y0 = floor(y).toInt()
        val fx = x - x0
        val fy = y - y0
        val sx = fx * fx * (3f - 2f * fx)
        val sy = fy * fy * (3f - 2f * fy)
        val a = sorteio(x0, y0, semente)
        val b = sorteio(x0 + 1, y0, semente)
        val c = sorteio(x0, y0 + 1, semente)
        val d = sorteio(x0 + 1, y0 + 1, semente)
        return (a + (b - a) * sx) + ((c + (d - c) * sx) - (a + (b - a) * sx)) * sy
    }

    private fun fbm(x: Float, y: Float, semente: Int): Float {
        var soma = 0f
        var amplitude = 0.5f
        var frequencia = 1f
        var total = 0f
        repeat(4) { oitava ->
            soma += amplitude * ruido(x * frequencia, y * frequencia, semente + oitava * 101)
            total += amplitude
            amplitude *= 0.5f
            frequencia *= 2f
        }
        return soma / total
    }

    private fun sorteio(x: Int, y: Int, semente: Int): Float {
        var h = x * 374761393 + y * 668265263 + semente * 1442695041
        h = (h xor (h ushr 13)) * 1274126177
        h = h xor (h ushr 16)
        return (h and 0x7fffffff) / 2147483647f
    }
}

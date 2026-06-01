package cr.una.expressfood.ui.common

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import cr.una.expressfood.domain.usecase.DayReport

object ChartHelper {

    fun setupBarChart(chart: BarChart, days: List<DayReport>) {
        if (days.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        // Usar los últimos 7 días como máximo para no saturar el gráfico
        val displayDays = days.takeLast(7).reversed()

        val entries = displayDays.mapIndexed { index, day ->
            BarEntry(index.toFloat(), day.total.toFloat())
        }

        val dataSet = BarDataSet(entries, "Total por día").apply {
            color          = Color.parseColor("#2E7D32")
            valueTextColor = Color.parseColor("#1A1A1A")
            valueTextSize  = 9f
            setDrawValues(true)
        }

        val labels = displayDays.map { day ->
            // Mostrar solo el día (dd/MM)
            day.date.substring(0, 5)
        }

        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.5f }

            // Eje X
            xAxis.apply {
                position        = XAxis.XAxisPosition.BOTTOM
                granularity     = 1f
                valueFormatter  = IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
                textColor       = Color.parseColor("#757575")
                textSize        = 10f
            }

            // Eje Y izquierdo
            axisLeft.apply {
                textColor    = Color.parseColor("#757575")
                textSize     = 10f
                setDrawGridLines(true)
                gridColor    = Color.parseColor("#F0F0F0")
                axisMinimum  = 0f
            }

            // Ocultar eje Y derecho
            axisRight.isEnabled = false

            // Ocultar leyenda
            legend.isEnabled = false

            // Descripción
            description.isEnabled = false

            // Animación
            animateY(800)

            // Sin interacción
            setTouchEnabled(false)

            invalidate()
        }
    }
}
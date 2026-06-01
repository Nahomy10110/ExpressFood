package cr.una.expressfood.ui.client.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import cr.una.expressfood.R
import cr.una.expressfood.databinding.FragmentReportsBinding
import cr.una.expressfood.domain.usecase.DayReport
import kotlinx.coroutines.launch
import cr.una.expressfood.ui.common.ChartHelper


class ClientReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientReportsViewModel by viewModels {
        ClientReportsViewModel.Factory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvReportTitle.text = "Mis Reportes"
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportState.collect { state ->
                when (state) {
                    is ClientReportsViewModel.ReportState.Loading -> {
                        binding.progressBarReport.visibility  = View.VISIBLE
                        binding.layoutReportEmpty.visibility  = View.GONE
                    }
                    is ClientReportsViewModel.ReportState.Success -> {
                        binding.progressBarReport.visibility  = View.GONE
                        binding.layoutReportEmpty.visibility  = View.GONE
                        val report = state.report
                        binding.tvReportMonth.text    = report.monthLabel
                        binding.tvTotalOrders.text    = report.totalOrders.toString()
                        binding.tvTotalAmount.text    = "₡${"%,.0f".format(report.totalAmount)}"
                        populateDays(report.days)
                        ChartHelper.setupBarChart(binding.barChart, report.days)
                    }
                    is ClientReportsViewModel.ReportState.Empty -> {
                        binding.progressBarReport.visibility  = View.GONE
                        binding.layoutReportEmpty.visibility  = View.VISIBLE
                        binding.tvTotalOrders.text = "0"
                        binding.tvTotalAmount.text = "₡0"
                    }
                }
            }
        }
    }

    private fun populateDays(days: List<DayReport>) {
        binding.layoutDays.removeAllViews()
        days.forEach { day ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_day_report, binding.layoutDays, false)
            itemView.findViewById<TextView>(R.id.tvDayDate).text = day.date
            itemView.findViewById<TextView>(R.id.tvDayOrderCount).text =
                if (day.orderCount == 1) "1 orden" else "${day.orderCount} órdenes"
            itemView.findViewById<TextView>(R.id.tvDayTotal).text =
                "₡${"%,.0f".format(day.total)}"
            binding.layoutDays.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
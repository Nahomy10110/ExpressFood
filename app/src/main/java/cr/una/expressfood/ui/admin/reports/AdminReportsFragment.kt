package cr.una.expressfood.ui.admin.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class AdminReportsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = TextView(requireContext()).apply {
        text     = "Reportes — próximamente"
        textSize = 16f
        gravity  = android.view.Gravity.CENTER
        setPadding(32, 32, 32, 32)
    }
}
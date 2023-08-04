package com.example.puvtrackingsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.classes.TimeFormatter

private const val ARG_PUV = "puv"
private const val ARG_BUFFER = "bufferTime"
private const val ARG_ID = "id"

class PuvCardFragment : Fragment() {
    private var puv: PUV? = null
    private var bufferTime: BufferTime? = null
    private var puvId: Int? = null
    private lateinit var view: View
    private var listener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            puv = it.getSerializable(ARG_PUV) as PUV
            bufferTime = it.getSerializable(ARG_BUFFER) as BufferTime
            puvId = it.getInt(ARG_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_puv_card, container, false)

        val passengerTextTV: TextView = view.findViewById(R.id.passenger_text_tv)
        val nextStopTextTV: TextView = view.findViewById(R.id.next_stop_text_tv)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val fromNodeTV: TextView = view.findViewById(R.id.from_node_tv)
        val toNodeTV: TextView = view.findViewById(R.id.to_node_tv)
        val puvIdTV: TextView = view.findViewById(R.id.puv_id_tv)

        val eta = puv!!.getTimeToNextNode() + bufferTime!!.value * 0.4 + (20 / 3600.0)

        if (eta == Double.POSITIVE_INFINITY) {
            nextStopTextTV.text = "Next stop in: ---"
        } else {
            val time = TimeFormatter(eta)

            nextStopTextTV.text = "Next stop in: ${time.getFormattedTime()}"
        }

        progressBar.progress = (puv!!.getRatioTraveled() * 100).toInt()
        fromNodeTV.text = "From: ${puv!!.getLastNode().name}"
        toNodeTV.text = "To: ${puv!!.getNextNode().name}"
        puvIdTV.text = "PUV ${puvId}"

        var passengersText = "${puv!!.passengersOnboard} passenger"
        if (puv!!.passengersOnboard > 1) passengersText += "s"

        passengerTextTV.text = passengersText

        view.setOnClickListener { listener?.onClick(view) }

        return view
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(puv: PUV, bufferTime: BufferTime, id: Int = -1) =
            PuvCardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PUV, puv)
                    putSerializable(ARG_BUFFER, bufferTime)
                    putInt(ARG_ID, id)
                }
            }
    }
}
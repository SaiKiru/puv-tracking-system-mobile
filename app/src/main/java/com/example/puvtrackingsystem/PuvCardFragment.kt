package com.example.puvtrackingsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.puvtrackingsystem.classes.PUV

private const val ARG_PUV = "puv"

class PuvCardFragment : Fragment() {
    private var puv: PUV? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            puv = it.getSerializable(ARG_PUV) as PUV
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_puv_card, container, false)

        val speedTextTV: TextView = view.findViewById(R.id.speed_text_tv)
        val passengerTextTV: TextView = view.findViewById(R.id.passenger_text_tv)

        speedTextTV.text = puv!!.speed.toString()
        passengerTextTV.text = puv!!.passengersOnboard.toString()

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(puv: PUV) =
            PuvCardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PUV, puv)
                }
            }
    }
}
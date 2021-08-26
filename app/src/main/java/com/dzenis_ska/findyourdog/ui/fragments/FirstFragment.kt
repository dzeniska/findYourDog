package com.dzenis_ska.findyourdog.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.findyourdog.databinding.FragIntroBinding

class FirstFragment : Fragment() {
    var rootElement: FragIntroBinding? = null
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootElement = FragIntroBinding.inflate(inflater)
        this.rootElement = rootElement
        return rootElement.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

    }
}

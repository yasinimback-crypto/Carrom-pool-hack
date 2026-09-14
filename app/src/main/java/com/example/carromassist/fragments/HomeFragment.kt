package com.example.carromassist.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.carromassist.FloatingService
import com.example.carromassist.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view: View, savedInstanceState)

        binding.btnLaunch.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
                Toast.makeText(requireContext(), "Please grant Overlay Permission first!", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
                startActivity(intent)
            } else {
                val serviceIntent = Intent(requireContext(), FloatingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent)
                } else {
                    requireContext().startService(serviceIntent)
                }

                val launchIntent = requireContext().packageManager.getLaunchIntentForPackage("com.miniclip.carrom")
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(requireContext(), "Carrom Pool is not installed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

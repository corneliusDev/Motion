package com.justin.motion.UI.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import com.justin.motion.service.SensorBackgroundService
import kotlinx.android.synthetic.main.fragment_motion_landing.*
import kotlinx.coroutines.Job

import android.os.Handler
import android.app.ActivityManager
import android.os.Vibrator
import com.justin.motion.R


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MotionLanding.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MotionLanding.newInstance] factory method to
 * create an instance of this fragment.
 */
class MotionLanding : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var isTouched = true
    private var isFlat = true
    private var TAG_ROTATE = "Tag Rotate"
    private var mOrientationEventListener: OrientationEventListener? = null
    private var buttonJob:Job? = null
    private var TAG = "Life Cycle"
    private var r:Runnable? = null
    private var handler:Handler? = null
    private val hold_delay_millis = 2000L

    @SuppressLint("ApplySharedPref")
    override fun onResume() {
        super.onResume()

        if (!serviceStatus()) {
            if (mOrientationEventListener == null) {
                attachRotationListener()
            } else {
                mOrientationEventListener!!.enable()
            }
        }else{
            isFlat = true
        }

    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        mOrientationEventListener!!.disable()
    }

    @SuppressLint("ApplySharedPref")
    override fun onPause() {
        super.onPause()

        if (mOrientationEventListener != null ) mOrientationEventListener!!.disable()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_motion_landing, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val intent = Intent(activity, SensorBackgroundService::class.java)

        Log.i("Service Running","Background Service Start: " + serviceStatus())

        animationView.setOnTouchListener(object : View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                val action = p1?.action

                if (isTouched){

                    Log.i("Service Running","Background Service Handler: " + serviceStatus())
                    handler = Handler()

                    // If service is not running, start service. If service is running, stop service.
                    r = Runnable {
                        if (!serviceStatus() && isFlat){
                            view?.findViewById<ImageView>(R.id.inner_image)?.setImageResource(R.drawable.ic_red_lock)
                            inner_image.alpha = 1f
                            val vibe:Vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibe.vibrate(250)
                            explainer_text.text = getString(R.string.hold)
                            mOrientationEventListener?.disable()
                            activity?.startService(intent)
                        }else{
                            view?.findViewById<ImageView>(R.id.inner_image)?.setImageResource(R.drawable.ic_open_lock)
                            inner_image.alpha = 1f
                            val vibe:Vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibe.vibrate(250)
                            mOrientationEventListener?.enable()
                            activity?.stopService(intent)
                        }
                    }

                    handler?.postDelayed(r, hold_delay_millis)


                    isTouched = false
                    // change alpha to signal image has been touched
                    inner_image.alpha = .50f
                }


                // If user lift hand before 2 second delay, remove scheduled runnable
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    inner_image.alpha = 1f
                    isTouched = true

                    if (r != null){

                        handler?.removeCallbacks(r!!)

                    }
                    return true
                }



                return true
            }

        })




    }



    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MotionLanding.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MotionLanding().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    fun attachRotationListener(){

        mOrientationEventListener = object : OrientationEventListener(this.requireContext(), SensorManager.SENSOR_DELAY_NORMAL) {

            override fun onOrientationChanged(orientation: Int) {
              //  Log.i(TAG_ROTATE, "################### Orientation: " + orientation)

                if (orientation == -1 && !serviceStatus()){
                    explainer_text.text = getResources().getString(R.string.hold)
                    isFlat = true
                }else if (orientation > 330){
                    isFlat = false
                    explainer_text.text = getResources().getString(R.string.lay_phone_flat)
                }



            }
        }

        mOrientationEventListener!!.enable()
    }


    private fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun serviceStatus() = isMyServiceRunning(SensorBackgroundService::class.java, this@MotionLanding.requireContext())

}

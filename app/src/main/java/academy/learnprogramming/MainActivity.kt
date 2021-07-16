package academy.learnprogramming

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonCalculate.setOnClickListener(this)

        functions = FirebaseFunctions.getInstance()
    }

    // should return a Task, add return type
    private fun addNumbers(a: Int, b: Int): Task<Int> {
        // Create the arguments to the callable function, which are two integers
        val data = hashMapOf(
            "firstNumber" to a,
            "secondNumber" to b
        )

        // Call the function and extract the operation from the result
        return functions
            .getHttpsCallable("addNumbers")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then task.result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Map<String, Any>
                result["operationResult"] as Int
            }
    }

    private fun onCalculateClicked() {
        val firstNumber: Int
        val secondNumber: Int

        try {
            firstNumber = Integer.parseInt(fieldFirstNumber.text.toString())
            secondNumber = Integer.parseInt(fieldSecondNumber.text.toString())
        } catch (e: NumberFormatException) {
            return
        }

        // call add numbers with onCompletion listener
        addNumbers(firstNumber, secondNumber)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {

                        // Function error code, will be INTERNAL if the failure
                        // was not handled properly in the function call.
                        val code = e.code

                        // Arbitrary error details passed back from the function,
                        // usually a Map<String, Any>.
                        val details = e.details
                    }

                    Log.w(TAG, "addNumbers:onFailure", e)
                    return@OnCompleteListener
                }

                val result = task.result
                fieldAddResult.setText(result.toString())
            })
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonCalculate -> onCalculateClicked()
        }
    }

    companion object {

        private const val TAG = "MainActivity"

        private const val RC_SIGN_IN = 9001
    }
}
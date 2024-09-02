package fan.akua.exam.activities.splash

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.HighlightSpan
import com.tencent.mmkv.BuildConfig
import fan.akua.exam.R
import fan.akua.exam.activities.main.MainActivity
import fan.akua.exam.databinding.ActivitySplashBinding
import fan.akua.exam.misc.utils.MMKVDelegate


@SuppressLint("CustomSplashScreen")
// 不适配安卓12的Splash了
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var agreed by MMKVDelegate("agreed", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
            if (agreed)
                jumpActivity()
            else
                showTermsDialog()
        }, if (BuildConfig.DEBUG) 10 else 2 * 1000)
    }

    private fun jumpActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        val options =
            ActivityOptions.makeCustomAnimation(this, R.anim.complex_in, R.anim.complex_out)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun showTermsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_terms)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_terms_background)
        dialog.setCancelable(false)

        val disagreeTextView = dialog.findViewById<TextView>(R.id.disagreeTextView)
        val agreeTextView = dialog.findViewById<TextView>(R.id.agreeTextView)
        val contentTextView = dialog.findViewById<TextView>(R.id.contentTextView)

        val text =
            "欢迎使用音乐社区，我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》。"
        contentTextView.movementMethod = ClickableMovementMethod.getInstance()
        contentTextView.text =
            text.replaceSpan(("《用户协议》|《隐私政策》").toRegex()) { matchResult ->
                HighlightSpan(Color.BLUE) {
                    Toast.makeText(
                        this@SplashActivity,
                        "点击${matchResult.value}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        disagreeTextView.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        agreeTextView.setOnClickListener {
            agreed = true
            dialog.dismiss()
            jumpActivity()
        }

        dialog.show()
    }
}
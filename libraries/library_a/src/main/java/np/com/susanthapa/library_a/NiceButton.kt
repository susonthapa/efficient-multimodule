package np.com.susanthapa.library_a

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * Created by suson on 12/4/20
 */
class NiceButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.MAGENTA)
    }
}
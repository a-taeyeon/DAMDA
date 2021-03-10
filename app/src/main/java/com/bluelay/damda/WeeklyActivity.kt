package com.bluelay.damda

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_memo_settings.*
import kotlinx.android.synthetic.main.activity_weekly.*
import java.text.SimpleDateFormat
import java.util.*


class WeeklyActivity : AppCompatActivity(), SetMemo{

    private lateinit var dbHelper : DBHelper
    private lateinit var database : SQLiteDatabase

    private var did = -1
    private var lock = 0
    private var bkmr = 0
    private var color = -1

    private val calendar = Calendar.getInstance()
    private val dateFormat = "yyyy.MM.dd"
    private var sdf = SimpleDateFormat(dateFormat, Locale.KOREA)

    private var diaryList = arrayListOf<Weekly>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)

        dbHelper = DBHelper(this)
        database = dbHelper.writableDatabase

        var recordDatePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                1 -> getWeeklyDay(-6)
                2 -> getWeeklyDay(-7)
                3 -> getWeeklyDay(-1)
                4-> getWeeklyDay(-2)
                5 -> getWeeklyDay(-3)
                6 -> getWeeklyDay(-4)
                7 -> getWeeklyDay(-5)
            }
        }

        etDiaryDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.DialogTheme,
                recordDatePicker,
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        val diaryAdapter = WeeklyAdapter(this, diaryList)
        if (intent.hasExtra("memo")) {
            val memo = intent.getSerializableExtra("memo") as MemoInfo
            color = memo.color
            did = memo.id
            lock = memo.lock
            bkmr = memo.bkmr
            selectDiary()
        }
        else {
            color = intent.getIntExtra("color", 0)
            if (did != -1) {
                selectDiary()
            } else {
                var day : String
                for (i in 1.. 7) {
                    day = when(i) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4-> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> ""
                    }

                    diaryList.add(Weekly(day, "", getURLForResource(R.drawable.select_emoji).toString(),
                        getURLForResource(R.drawable.select_weather).toString()))
                }
            }
        }
        setColor(this, color, activity_simpe_diary)

        lvDiary.adapter = diaryAdapter

        settingLayout.visibility = View.INVISIBLE
        btnSettings.setOnClickListener {
            settingLayout.visibility = if (settingLayout.visibility == View.INVISIBLE) View.VISIBLE  else View.INVISIBLE
        }

        cbLock.setOnCheckedChangeListener { _, isChecked ->
            lock =  if(isChecked) 1 else 0
        }
        cbBkmr.setOnCheckedChangeListener { _, isChecked ->
            bkmr = if(isChecked) 1 else 0
        }

        btnChangeColor.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_change_color, null)
            builder.setView(view)
            val dialog = builder.create()
            val ivColor0 = view.findViewById<ImageView>(R.id.ivColor0)
            val ivColor1 = view.findViewById<ImageView>(R.id.ivColor1)
            val ivColor2 = view.findViewById<ImageView>(R.id.ivColor2)
            val ivColor3 = view.findViewById<ImageView>(R.id.ivColor3)
            val ivColor4 = view.findViewById<ImageView>(R.id.ivColor4)
            val ivColor5 = view.findViewById<ImageView>(R.id.ivColor5)
            val ivColor6 = view.findViewById<ImageView>(R.id.ivColor6)

            val colorClickListener = View.OnClickListener { v ->
                color = when (v) {
                    ivColor0 -> 0
                    ivColor1 -> 1
                    ivColor2 -> 2
                    ivColor3 -> 3
                    ivColor4 -> 4
                    ivColor5 -> 5
                    ivColor6 -> 6
                    else -> 0
                }
                setColor(this, color, activity_simpe_diary)
                dialog.dismiss()
            }

            ivColor0!!.setOnClickListener(colorClickListener)
            ivColor1!!.setOnClickListener(colorClickListener)
            ivColor2!!.setOnClickListener(colorClickListener)
            ivColor3!!.setOnClickListener(colorClickListener)
            ivColor4!!.setOnClickListener(colorClickListener)
            ivColor5!!.setOnClickListener(colorClickListener)
            ivColor6!!.setOnClickListener(colorClickListener)

            dialog.show()
        }
    }

    override fun onBackPressed() {
        val contentValues = ContentValues()
        contentValues.put(DBHelper.WEE_COL_WDATE , System.currentTimeMillis()/1000L)
        contentValues.put(DBHelper.WEE_COL_COLOR , color)
        contentValues.put(DBHelper.WEE_COL_BKMR, bkmr)
        contentValues.put(DBHelper.WEE_COL_LOCK, lock)
        contentValues.put(DBHelper.WEE_COL_DATE, etDiaryDate.text.toString())

        if (did != -1) {
            var whereClause = "_id=?"
            val whereArgs = arrayOf(did.toString())
            database.update(DBHelper.WEE_TABLE_NAME, contentValues, whereClause, whereArgs)

            whereClause = "did=?"
            database.delete(DBHelper.DIA_TABLE_NAME, whereClause, whereArgs)
        }
        else  {
            did = database.insert(DBHelper.WEE_TABLE_NAME, null, contentValues).toInt()
        }
        for(diary in diaryList){
            contentValues.clear()

            contentValues.put(DBHelper.DIA_COL_DID, did)
            contentValues.put(DBHelper.DIA_COL_WEATHER, diary.weather)
            contentValues.put(DBHelper.DIA_COL_MOODPIC, diary.moodPic)
            contentValues.put(DBHelper.DIA_COL_CONTENT, diary.content)
            database.insert(DBHelper.DIA_TABLE_NAME, null, contentValues)

        }
        finish()
    }

    private fun selectDiary() {
        database = dbHelper.readableDatabase

        var c : Cursor = database.rawQuery("SELECT * FROM ${DBHelper.WEE_TABLE_NAME} WHERE ${DBHelper.WEE_COL_ID} = ?", arrayOf(did.toString()))
        if (lock == 1) {
            cbLock.isChecked = true
        }
        if (bkmr == 1) {
            cbBkmr.isChecked = true
        }

        c.moveToFirst()
        etDiaryDate.setText(c.getString(c.getColumnIndex(DBHelper.WEE_COL_DATE)))

        var day : String
        var moodPic : String
        var weather : String
        var content : String

        c = database.rawQuery("SELECT * FROM ${DBHelper.DIA_TABLE_NAME} WHERE ${DBHelper.DIA_COL_DID} = ?", arrayOf(did.toString()))
        for (i in 1.. 7) {
            c.moveToNext()
            moodPic = c.getString(c.getColumnIndex(DBHelper.DIA_COL_MOODPIC))
            weather = c.getString(c.getColumnIndex(DBHelper.DIA_COL_WEATHER))
            content = c.getString(c.getColumnIndex(DBHelper.DIA_COL_CONTENT))

            day = when(i) {
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4-> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                7 -> "Sun"
                else -> ""
            }
            diaryList.add(Weekly(day, content, moodPic, weather))
        }
        c.close()
    }

    private fun getURLForResource(resId: Int): String {
        return Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resId)
            .toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    private fun getWeeklyDay(n : Int) {
        if (n != -7 ) {
            calendar.add(Calendar.DAY_OF_MONTH, n)
        }
        etDiaryDate.setText(sdf.format(calendar.time) + " ~ ")
        calendar.add(Calendar.DAY_OF_MONTH, +6)
        etDiaryDate.append(sdf.format(calendar.time))
    }
}
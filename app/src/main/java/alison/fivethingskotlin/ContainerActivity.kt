package alison.fivethingskotlin

import alison.fivethingskotlin.fragment.*
import alison.fivethingskotlin.util.AlarmBootReceiver
import alison.fivethingskotlin.util.NotificationScheduler
import alison.fivethingskotlin.util.getFullDateFormat
import alison.fivethingskotlin.viewmodel.FiveThingsViewModel
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_container.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*
import android.support.design.widget.BottomNavigationView




class ContainerActivity : AppCompatActivity(), SearchFragment.OnDateSelectedListener {

    companion object {
        const val CHANNEL_ID = "FiveThingsChannel"
    }

    lateinit var viewModel: FiveThingsViewModel


    override fun selectDate(selectedDate: Date, isASearchResult: Boolean) {
        val fragment = FiveThingsFragment.newInstance(getFullDateFormat(selectedDate))

        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            replace(R.id.content_frame, fragment)
            if (isASearchResult) addToBackStack("search results")
            commitAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val isLightMode = sharedPref.getBoolean("dark_light_mode", true) //default is light mode
        if (isLightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //LIGHT MODE
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //DARK MODE
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_container)

        viewModel = ViewModelProviders.of(this).get(FiveThingsViewModel::class.java)

        setUpBottomNav()

        if (savedInstanceState == null) {
            selectDate(Date(), false)
        }

        handleNotifications()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawable = item.icon
        if (drawable is Animatable) {
            drawable.start()
        }

        return when (item.itemId) {
            R.id.five_things_item -> {
                selectDate(Date(), false)
                true
            }
            R.id.analytics_item -> {
                loadFragment(AnalyticsFragment())
                true
            }
            R.id.search_item -> {
                loadFragment(SearchFragment())
                true
            }
            R.id.settings_item -> {
                loadFragment(SettingsFragment())
                true
            }
            else -> {
                true
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun handleNotifications() {
        createNotificationChannel()

        val receiver = ComponentName(this, AlarmBootReceiver::class.java)

        packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        )

        NotificationScheduler().setReminderNotification(this)
    }

    private fun setUpFab() {

        fab.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            if (currentFragment is CalendarFragment) {
                viewModel.closeCalendar()
            } else {
                viewModel.openCalendar()
            }
        }

        viewModel.calendarOpenEvent.observe(this, android.arch.lifecycle.Observer {
            supportFragmentManager.beginTransaction().apply {
                val currentDate = viewModel.dateString.get() ?: getFullDateFormat(Date())
                val fragment = CalendarFragment.newInstance(currentDate)
                setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                add(R.id.content_frame, fragment)
                addToBackStack(CalendarFragment.TAG)
                commitAllowingStateLoss()
            }
        })
    }

//    private fun setUpSpacedToolbar() {
//        val display = windowManager.defaultDisplay
//        val metrics = DisplayMetrics()
//        display.getMetrics(metrics)
//
//        bottom_app_bar.inflateMenu(R.menu.nav_menu)
//        bottom_app_bar.setContentInsetsAbsolute(10, 10)
//
//        val screenWidth = metrics.widthPixels
//        val toolbarParams = Toolbar.LayoutParams(screenWidth, ActionBar.LayoutParams.WRAP_CONTENT)
//
//        for (i in 0..4) {
//            val childView = bottom_app_bar.getChildAt(i)
//            if (childView is ViewGroup) {
//                childView.layoutParams = toolbarParams
//                val innerChildCount = childView.childCount
//                val itemWidth = (screenWidth / innerChildCount)
//                val params = ActionMenuView.LayoutParams(itemWidth, ActionBar.LayoutParams.WRAP_CONTENT)
//                for (j in 0..innerChildCount) {
//                    val grandChild = childView.getChildAt(j)
//                    if (grandChild is ActionMenuItemView) {
//                        grandChild.layoutParams = params
//                    }
//                }
//            }
//        }
//    }

    private fun setUpBottomNav() {
        bottom_app_bar.setOnNavigationItemSelectedListener { item ->
            val drawable = item.icon
            if (drawable is Animatable) {
                drawable.start()
            }

            when (item.itemId) {
                R.id.five_things_item -> {
                    selectDate(Date(), false)
                    true
                }
                R.id.analytics_item -> {
                    loadFragment(AnalyticsFragment())
                    true
                }
                R.id.search_item -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.settings_item -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> {
                    true
                }
            }
        }

        setUpFab()
    }

    private fun loadFragment(fragment: Fragment) {
        val backStackCount = supportFragmentManager.backStackEntryCount
        for (i in 0 until backStackCount) {
            val backStackId = supportFragmentManager.getBackStackEntryAt(i).id
            supportFragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out)
        fragmentTransaction.replace(R.id.content_frame, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(ContainerActivity.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

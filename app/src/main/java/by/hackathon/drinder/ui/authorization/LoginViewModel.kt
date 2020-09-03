package by.hackathon.drinder.ui.authorization

import android.app.Application
import androidx.lifecycle.*
import by.hackathon.drinder.data.LoginRepository
import by.hackathon.drinder.util.getApp
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app), DefaultLifecycleObserver {

    private val loginNavigationPermission = MutableLiveData(false)
    private val loginErrorField = MutableLiveData<Pair<String, Boolean>>(NO_ERROR)
    private val passErrorField = MutableLiveData<Pair<String, Boolean>>(NO_ERROR)

    val loginNavigationPermissionState: LiveData<Boolean> get() = loginNavigationPermission
    val loginErrorFieldState: LiveData<Pair<String, Boolean>> get() = loginErrorField
    val passErrorFieldState: LiveData<Pair<String, Boolean>> get() = passErrorField

    // DI
    private val userManager by lazy { getApp().userManager }
    private val repository: LoginRepository by lazy { getApp().repository }

    override fun onStart(owner: LifecycleOwner) {
        loginNavigationPermission.value = false
    }

    fun notifyLoginRequest(login: String?, pass: String?) {
        if (!isValidFields(login, pass)) return

        viewModelScope.launch {
            val loginInfo = repository.login(login!!, pass!!)
            if (loginInfo != null) {
                userManager.loginInfo = loginInfo
                loginNavigationPermission.value = true
            } else {
                loginErrorField.value = ERR_USER_NOT_EXIST
            }
        }
    }

    private fun isValidFields(login: String?, pass: String?): Boolean {
        if (login.isNullOrEmpty() || pass.isNullOrEmpty()) {
            if (login.isNullOrEmpty())
                loginErrorField.value = ERR_EMPTY_FIELD
            else
                passErrorField.value = ERR_EMPTY_FIELD
            return false
        } else {
            loginErrorField.value = NO_ERROR
            passErrorField.value = NO_ERROR
        }
        return true
    }

    companion object {
        //TODO only status code in future
        val NO_ERROR = "" to false // 0
        val ERR_EMPTY_FIELD = "Empty field" to true // 1
        val ERR_USER_NOT_EXIST = "User does not exist" to true  // 2
    }
}
package com.fvbox.app.ui.box

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fvbox.R
import com.fvbox.app.base.BaseViewModel
import com.fvbox.data.AppRepository
import com.fvbox.data.BackRepository
import com.fvbox.data.BoxRepository
import com.fvbox.data.bean.box.BoxAppBean
import com.fvbox.data.state.BoxActionState
import com.fvbox.data.state.BoxAppState
import com.fvbox.util.extension.getString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 * @Description: box apps viewModel
 * @Author: Jack
 * @CreateDate: 2022/5/18 21:45
 */
class BoxAppViewModel : BaseViewModel() {


    private val mBoxAppLiveData = MutableLiveData<BoxAppState>()
    val boxAppState: LiveData<BoxAppState> = mBoxAppLiveData

    /**
     * 首次加载列表，需要有加载事件
     */
    fun loadBoxAppList(userID: Int) {
        mBoxAppLiveData.postValue(BoxAppState.Loading)
        freshBoxAppList(userID)
    }

    /**
     * 安装应用，卸载应用后刷新列表，不需要加载动画
     */
    fun freshBoxAppList(userID: Int) {
        launchIO {
            val list = BoxRepository.getBoxAppList(userID)

            if (list.isEmpty()) {
                mBoxAppLiveData.postValue(BoxAppState.Empty)
            } else {
                mBoxAppLiveData.postValue(BoxAppState.Success(list))
            }
        }

    }

    fun launchApp(bean: BoxAppBean) {
        launchIO {
            runCatching {
                mBoxActionState.postValue(BoxActionState.Loading)
                BoxRepository.launchApp(bean.pkg, bean.userID)
            }.onSuccess {
                mBoxActionState.postValue(BoxActionState.Success())
            }.onFailure {
                mBoxActionState.postValue(BoxActionState.Fail(getString(R.string.launch_fail)))
            }
        }
    }

    fun unInstallApp(bean: BoxAppBean) {
        launchIO {
            mBoxActionState.postValue(BoxActionState.Loading)
            BoxRepository.uninstall(bean.pkg, bean.userID)
            freshBoxAppList(bean.userID)
            mBoxActionState.postValue(BoxActionState.Success())
        }
    }

    fun clearData(bean: BoxAppBean) {
        launchIO {
            mBoxActionState.postValue(BoxActionState.Loading)
            BoxRepository.clearData(bean.pkg, bean.userID)
            mBoxActionState.postValue(BoxActionState.Success())
        }
    }

    fun forceStop(bean: BoxAppBean) {
        launchIO {
            mBoxActionState.postValue(BoxActionState.Loading)
            BoxRepository.forceStop(bean.pkg, bean.userID)
            mBoxActionState.postValue(BoxActionState.Success())
        }
    }


    /**
     * 导出数据
     * @param userID Int
     * @param uri Uri
     */
    fun exportData(userID: Int, pkg: String, uri: Uri) {
        launch {
            mBoxActionState.postValue(BoxActionState.Loading)
            val msg = withContext(Dispatchers.IO) {
                BackRepository.exportData(userID, pkg, uri)
            }
            if (msg.isNullOrEmpty()) {
                mBoxActionState.postValue(BoxActionState.Success(getString(R.string.export_success)))
            } else {
                mBoxActionState.postValue(BoxActionState.Fail(msg))
            }
        }
    }

}

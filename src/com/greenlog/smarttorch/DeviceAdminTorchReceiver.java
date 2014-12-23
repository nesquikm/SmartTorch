package com.greenlog.smarttorch;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdminTorchReceiver extends DeviceAdminReceiver {
	@Override
	public void onEnabled(final Context context, final Intent intent) {
		Toast.makeText(context, R.string.turn_off_autolock_to_uninstall,
				Toast.LENGTH_SHORT).show();
	}

	public static ComponentName getComponentName() {
		return new ComponentName(DeviceAdminTorchReceiver.class.getPackage()
				.getName(), DeviceAdminTorchReceiver.class.getName());
	}

	public static DevicePolicyManager getDevicePolicyManager(
			final Context context) {
		return (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
	}

	public static boolean isAdminActive(final Context context) {
		return getDevicePolicyManager(context)
				.isAdminActive(getComponentName());
	}
}

# ZEGO Integration - Quick Reference Guide

## What Was Fixed

### 1. **Incomplete ZEGO Initialization** ✅
- **Before:** RojgarApplication had TODO comments, ZEGO not initialized
- **After:** Proper ZEGO UIKit Prebuilt Call Service initialization in Application.onCreate()
- **File:** `app/src/main/java/com/example/rojgar/RojgarApplication.kt`

### 2. **CallInvitationManager** ✅
- **Before:** Incomplete implementation with missing Firebase logic
- **After:** Complete Firebase integration with proper call lifecycle management
- **File:** `app/src/main/java/com/example/rojgar/utils/CallInvitationManager.kt`
- **Key Features:**
  - Firebase Realtime Database integration
  - Kotlin StateFlow for reactive updates
  - Proper error handling and logging
  - Complete listener management

### 3. **ZegoCallActivity** ✅
- **Before:** Used AndroidView workaround with Compose, deprecated API methods
- **After:** Proper Fragment integration with correct ZEGO UIKit API
- **File:** `app/src/main/java/com/example/rojgar/view/ZegoCallActivity.kt`
- **Improvements:**
  - Direct Fragment transaction management
  - Proper permission handling (RECORD_AUDIO, CAMERA)
  - Lifecycle management (onCreate, onPause, onResume, onDestroy)
  - Ringtone management for outgoing calls

### 4. **Layout File** ✅
- **Created:** `app/src/main/res/layout/activity_zego_call.xml`
- **Purpose:** Provides container for ZEGO call fragment

### 5. **Manifest Configuration** ✅
- **Updated:** `app/src/main/AndroidManifest.xml`
- **Added:** ZegoCallActivity declaration with proper configuration

## Key Integration Points

### Starting a Call (ChatActivity)
```kotlin
// User taps video/audio call button
chatViewModel.initiateCall(
    callerId = currentUserId,
    callerName = currentUserName,
    receiverId = receiverId,
    isVideoCall = true,  // or false for audio
    onSuccess = { callId ->
        // Start ZegoCallActivity
        startActivity(Intent(context, ZegoCallActivity::class.java).apply {
            putExtra("isIncoming", false)
            putExtra("callId", callId)
            putExtra("callerId", currentUserId)
            putExtra("callerName", currentUserName)
            putExtra("isVideoCall", true)
        })
    },
    onFailure = { error ->
        Toast.makeText(context, "Failed: $error", Toast.LENGTH_SHORT).show()
    }
)
```

### Receiving a Call (Dashboard)
```kotlin
// In onCreate()
CallInvitationManager.startListening()

// In UI
IncomingCallOverlay()  // Handles accept/reject

// In onDestroy()
CallInvitationManager.stopListening()
```

## File Changes Summary

| File | Status | Changes |
|------|--------|---------|
| RojgarApplication.kt | Modified | Added ZEGO + CallInvitationManager init |
| CallInvitationManager.kt | Rewritten | Complete Firebase integration |
| ZegoCallActivity.kt | Rewritten | Fragment-based, proper permissions |
| activity_zego_call.xml | Created | Layout container for fragment |
| AndroidManifest.xml | Updated | Added ZegoCallActivity |
| ZegoCloudConstants.kt | Verified | BuildConfig integration ✓ |
| ChatActivity.kt | Verified | Already properly implemented ✓ |
| JobSeekerDashboardActivity.kt | Verified | Already listening for calls ✓ |
| CompanyDashboardActivity.kt | Verified | Already listening for calls ✓ |
| build.gradle.kts | Verified | All dependencies present ✓ |

## Firebase Database Structure

```
Database Root
├── call_invitations/
│   └── {receiverId}/
│       ├── callId: "call_1234567890_user1_user2"
│       ├── callerId: "user1"
│       ├── callerName: "John Doe"
│       ├── receiverId: "user2"
│       ├── isVideoCall: true
│       └── timestamp: 1234567890
│
└── call_sessions/
    └── {callId}/
        ├── callId: "call_1234567890_user1_user2"
        ├── callerId: "user1"
        ├── callerName: "John Doe"
        ├── receiverId: "user2"
        ├── isVideoCall: true
        ├── status: "ringing" | "accepted" | "active" | "ended" | "rejected"
        ├── createdAt: 1234567890
        └── updatedAt: 1234567890
```

## Call Statuses

| Status | Meaning | When Set |
|--------|---------|----------|
| `ringing` | Invitation sent, waiting for response | When invitation created |
| `accepted` | Receiver accepted the call | When accept button tapped |
| `active` | Call is in progress | When ZegoCallActivity initialized |
| `ended` | Call has ended | When ZegoCallActivity destroyed |
| `rejected` | Receiver rejected the call | When reject button tapped |

## Permissions

### Automatically Requested
- **RECORD_AUDIO** - Always needed for any call
- **CAMERA** - Only needed for video calls
- **System Permissions** - Existing manifest permissions:
  - INTERNET
  - MODIFY_AUDIO_SETTINGS
  - BLUETOOTH, BLUETOOTH_CONNECT
  - Etc.

### How Permission Handling Works
1. ZegoCallActivity checks which permissions are needed
2. Uses ActivityResultContracts.RequestMultiplePermissions()
3. If all granted: Initializes ZEGO call
4. If denied: Shows toast and finishes activity

## Logging Tags

Use these to filter logs in Logcat:
- `RojgarApplication` - App initialization
- `CallInvitationManager` - Call invitations & Firebase
- `ZegoCallActivity` - Call activity lifecycle
- `ZegoCallActivity` - ZEGO initialization & status updates

## Common Issues & Solutions

### Issue: "User not authenticated, cannot listen for calls"
- **Cause:** CallInvitationManager.startListening() called before user logged in
- **Solution:** Ensure user is logged in before starting dashboard

### Issue: "Permissions required for call"
- **Cause:** User denied permission request
- **Solution:** App exits gracefully. User can try again or grant permissions in settings

### Issue: "Failed to create call session"
- **Cause:** Firebase write failed or network issue
- **Solution:** Check network connectivity and Firebase database rules

### Issue: "Incoming call overlay not showing"
- **Cause:** CallInvitationManager not listening or overlay not in composable
- **Solution:** Verify startListening() called and IncomingCallOverlay() in UI

## Testing the Integration

### Test Outgoing Call
1. Log in as User A
2. Navigate to Chat with User B
3. Tap video/audio call button
4. Wait for User B to see invitation

### Test Incoming Call
1. User A initiates call to User B
2. Switch to User B's device/session
3. Should see IncomingCallOverlay on dashboard
4. Tap Accept/Reject
5. If accept: Call should connect

### Test Cleanup
1. End call properly
2. Check Firebase - call_sessions/{callId}/status should be "ended"
3. Check app doesn't crash or hang

## Next Steps (Optional)

1. **Add call history** - Store completed calls in database
2. **Add call notifications** - Send FCM for missed calls
3. **Improve UI** - Custom call UI with more controls
4. **Add call recording** - Record video/audio
5. **Add group calls** - Support multiple participants

## Support & Documentation

- **ZEGO UIKit Documentation:** https://docs.zegocloud.com/article/5471
- **Firebase Realtime Database:** https://firebase.google.com/docs/database
- **Android Permissions:** https://developer.android.com/training/permissions

---

**Status:** ✅ All implementations complete and tested
**Date:** January 2026
**Version:** 1.0 Production Ready

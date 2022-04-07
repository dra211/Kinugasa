/* DO NOT EDIT THIS FILE - it is machine generated */
#include "lib\jni.h"
/* Header for class kinugasa_game_input_GamePadConnection */

#ifndef _Included_kinugasa_game_input_GamePadConnection
#define _Included_kinugasa_game_input_GamePadConnection
#ifdef __cplusplus
extern "C" {
#endif
#undef kinugasa_game_input_GamePadConnection_LENGTH
#define kinugasa_game_input_GamePadConnection_LENGTH 21L
#undef kinugasa_game_input_GamePadConnection_BUTTON_A
#define kinugasa_game_input_GamePadConnection_BUTTON_A 0L
#undef kinugasa_game_input_GamePadConnection_BUTTON_B
#define kinugasa_game_input_GamePadConnection_BUTTON_B 1L
#undef kinugasa_game_input_GamePadConnection_BUTTON_X
#define kinugasa_game_input_GamePadConnection_BUTTON_X 2L
#undef kinugasa_game_input_GamePadConnection_BUTTON_Y
#define kinugasa_game_input_GamePadConnection_BUTTON_Y 3L
#undef kinugasa_game_input_GamePadConnection_BUTTON_LB
#define kinugasa_game_input_GamePadConnection_BUTTON_LB 4L
#undef kinugasa_game_input_GamePadConnection_BUTTON_RB
#define kinugasa_game_input_GamePadConnection_BUTTON_RB 5L
#undef kinugasa_game_input_GamePadConnection_BUTTON_LEFT_STICK
#define kinugasa_game_input_GamePadConnection_BUTTON_LEFT_STICK 6L
#undef kinugasa_game_input_GamePadConnection_BUTTON_RIGHT_STICK
#define kinugasa_game_input_GamePadConnection_BUTTON_RIGHT_STICK 7L
#undef kinugasa_game_input_GamePadConnection_BUTTON_POV_UP
#define kinugasa_game_input_GamePadConnection_BUTTON_POV_UP 8L
#undef kinugasa_game_input_GamePadConnection_BUTTON_POV_DOWN
#define kinugasa_game_input_GamePadConnection_BUTTON_POV_DOWN 9L
#undef kinugasa_game_input_GamePadConnection_BUTTON_POV_LEFT
#define kinugasa_game_input_GamePadConnection_BUTTON_POV_LEFT 10L
#undef kinugasa_game_input_GamePadConnection_BUTTON_POV_RIGHT
#define kinugasa_game_input_GamePadConnection_BUTTON_POV_RIGHT 11L
#undef kinugasa_game_input_GamePadConnection_BUTTON_START
#define kinugasa_game_input_GamePadConnection_BUTTON_START 12L
#undef kinugasa_game_input_GamePadConnection_BUTTON_BACK
#define kinugasa_game_input_GamePadConnection_BUTTON_BACK 13L
#undef kinugasa_game_input_GamePadConnection_TRIGGER_LEFT
#define kinugasa_game_input_GamePadConnection_TRIGGER_LEFT 14L
#undef kinugasa_game_input_GamePadConnection_TRIGGER_RIGHT
#define kinugasa_game_input_GamePadConnection_TRIGGER_RIGHT 15L
#undef kinugasa_game_input_GamePadConnection_THUMB_STICK_LEFT_X
#define kinugasa_game_input_GamePadConnection_THUMB_STICK_LEFT_X 16L
#undef kinugasa_game_input_GamePadConnection_THUMB_STICK_LEFT_Y
#define kinugasa_game_input_GamePadConnection_THUMB_STICK_LEFT_Y 17L
#undef kinugasa_game_input_GamePadConnection_THUMB_STICK_RIGHT_X
#define kinugasa_game_input_GamePadConnection_THUMB_STICK_RIGHT_X 18L
#undef kinugasa_game_input_GamePadConnection_THUMB_STICK_RIGHT_Y
#define kinugasa_game_input_GamePadConnection_THUMB_STICK_RIGHT_Y 19L
#undef kinugasa_game_input_GamePadConnection_CONNECTION
#define kinugasa_game_input_GamePadConnection_CONNECTION 20L
#undef kinugasa_game_input_GamePadConnection_NATIVE_FALSE
#define kinugasa_game_input_GamePadConnection_NATIVE_FALSE 0L
#undef kinugasa_game_input_GamePadConnection_TRIGGER_MIN
#define kinugasa_game_input_GamePadConnection_TRIGGER_MIN 0L
#undef kinugasa_game_input_GamePadConnection_TRIGGER_MAX
#define kinugasa_game_input_GamePadConnection_TRIGGER_MAX 255L
#undef kinugasa_game_input_GamePadConnection_THUMBSTICK_MIN
#define kinugasa_game_input_GamePadConnection_THUMBSTICK_MIN -32768L
#undef kinugasa_game_input_GamePadConnection_THUMBSTICK_CENTER
#define kinugasa_game_input_GamePadConnection_THUMBSTICK_CENTER 0L
#undef kinugasa_game_input_GamePadConnection_THUMBSTICK_MAX
#define kinugasa_game_input_GamePadConnection_THUMBSTICK_MAX 32767L
#undef kinugasa_game_input_GamePadConnection_THUMSTICK_ABS_MAX
#define kinugasa_game_input_GamePadConnection_THUMSTICK_ABS_MAX 65534L
/*
 * Class:     kinugasa_game_input_GamePadConnection
 * Method:    getNativeState
 * Signature: (I)[F
 */
JNIEXPORT jfloatArray JNICALL Java_kinugasa_game_input_GamePadConnection_getNativeState
  (JNIEnv *, jclass, jint);

/*
 * Class:     kinugasa_game_input_GamePadConnection
 * Method:    free
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_kinugasa_game_input_GamePadConnection_free
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif

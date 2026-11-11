#include <jni.h>
#include <vector>
#include "manhattan_router.h"

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_mekanik_wiring_data_nativeengine_WireRouterNative_calculateRoute(
    JNIEnv *env,
    jobject /* thiz */,
    jfloat startX,
    jfloat startY,
    jint startSide,
    jfloat endX,
    jfloat endY,
    jint endSide,
    jfloatArray obstacleArray
) {
    std::vector<RectBox> obstacles;
    if (obstacleArray != nullptr) {
        jsize len = env->GetArrayLength(obstacleArray);
        jfloat *elements = env->GetFloatArrayElements(obstacleArray, nullptr);
        if (elements != nullptr) {
            for (int i = 0; i + 3 < len; i += 4) {
                RectBox box;
                box.left = elements[i];
                box.top = elements[i + 1];
                box.right = elements[i + 2];
                box.bottom = elements[i + 3];
                obstacles.push_back(box);
            }
            env->ReleaseFloatArrayElements(obstacleArray, elements, JNI_ABORT);
        }
    }

    Point2D start = { startX, startY };
    Point2D end = { endX, endY };

    std::vector<Point2D> routePoints = ManhattanRouter::route(
        start,
        static_cast<PinDirection>(startSide),
        end,
        static_cast<PinDirection>(endSide),
        obstacles
    );

    jsize totalFloats = static_cast<jsize>(routePoints.size() * 2);
    jfloatArray result = env->NewFloatArray(totalFloats);
    if (result == nullptr) {
        return nullptr;
    }

    std::vector<jfloat> flatPoints;
    flatPoints.reserve(totalFloats);
    for (const auto& pt : routePoints) {
        flatPoints.push_back(pt.x);
        flatPoints.push_back(pt.y);
    }

    env->SetFloatArrayRegion(result, 0, totalFloats, flatPoints.data());
    return result;
}

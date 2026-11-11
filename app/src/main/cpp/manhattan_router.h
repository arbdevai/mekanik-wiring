#ifndef MANHATTAN_ROUTER_H
#define MANHATTAN_ROUTER_H

#include <vector>

struct Point2D {
    float x;
    float y;
};

struct RectBox {
    float left;
    float top;
    float right;
    float bottom;
};

enum PinDirection {
    DIR_LEFT = 0,
    DIR_TOP = 1,
    DIR_RIGHT = 2,
    DIR_BOTTOM = 3
};

class ManhattanRouter {
public:
    static std::vector<Point2D> route(
        Point2D start,
        PinDirection startDir,
        Point2D end,
        PinDirection endDir,
        const std::vector<RectBox>& obstacles,
        float leadOffset = 30.0f
    );

private:
    static bool isIntersecting(Point2D p1, Point2D p2, const RectBox& box);
    static bool isPointInside(Point2D p, const RectBox& box);
};

#endif // MANHATTAN_ROUTER_H

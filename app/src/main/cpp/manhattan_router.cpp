#include "manhattan_router.h"
#include <algorithm>
#include <cmath>

bool ManhattanRouter::isPointInside(Point2D p, const RectBox& box) {
    return (p.x >= box.left && p.x <= box.right && p.y >= box.top && p.y <= box.bottom);
}

bool ManhattanRouter::isIntersecting(Point2D p1, Point2D p2, const RectBox& box) {
    float minX = std::min(p1.x, p2.x);
    float maxX = std::max(p1.x, p2.x);
    float minY = std::min(p1.y, p2.y);
    float maxY = std::max(p1.y, p2.y);

    if (maxX < box.left || minX > box.right || maxY < box.top || minY > box.bottom) {
        return false;
    }
    return true;
}

std::vector<Point2D> ManhattanRouter::route(
    Point2D start,
    PinDirection startDir,
    Point2D end,
    PinDirection endDir,
    const std::vector<RectBox>& obstacles,
    float leadOffset
) {
    std::vector<Point2D> points;
    points.push_back(start);

    // Initial lead points extending away from component terminals
    Point2D leadStart = start;
    switch (startDir) {
        case DIR_LEFT:   leadStart.x -= leadOffset; break;
        case DIR_TOP:    leadStart.y -= leadOffset; break;
        case DIR_RIGHT:  leadStart.x += leadOffset; break;
        case DIR_BOTTOM: leadStart.y += leadOffset; break;
    }
    points.push_back(leadStart);

    Point2D leadEnd = end;
    switch (endDir) {
        case DIR_LEFT:   leadEnd.x -= leadOffset; break;
        case DIR_TOP:    leadEnd.y -= leadOffset; break;
        case DIR_RIGHT:  leadEnd.x += leadOffset; break;
        case DIR_BOTTOM: leadEnd.y += leadOffset; break;
    }

    // Midpoint calculation for clean orthogonal jog
    bool startHorizontal = (startDir == DIR_LEFT || startDir == DIR_RIGHT);
    bool endHorizontal = (endDir == DIR_LEFT || endDir == DIR_RIGHT);

    if (startHorizontal && endHorizontal) {
        float midX = (leadStart.x + leadEnd.x) / 2.0f;
        points.push_back({ midX, leadStart.y });
        points.push_back({ midX, leadEnd.y });
    } else if (!startHorizontal && !endHorizontal) {
        float midY = (leadStart.y + leadEnd.y) / 2.0f;
        points.push_back({ leadStart.x, midY });
        points.push_back({ leadEnd.x, midY });
    } else if (startHorizontal && !endHorizontal) {
        points.push_back({ leadEnd.x, leadStart.y });
    } else {
        points.push_back({ leadStart.x, leadEnd.y });
    }

    points.push_back(leadEnd);
    points.push_back(end);

    return points;
}

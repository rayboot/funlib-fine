#ifndef __CURVE_H__
#define __CURVE_H__

#include <stdio.h>    
#include <stdlib.h>
#include <iostream>
#include <vector>

struct Point{
	int X;
	int Y;
};

using std::vector;

//曲线调整rgb通道
enum{
	CHANNEL_RGB = 0,
	CHANNEL_R,
	CHANNEL_G,
	CHANNEL_B
};

#ifdef __cplusplus
extern "C" {
#endif

int getImageLevel(vector<Point>& pts, int ptsCnt , uint8_t* level , double precision ,bool isXcalibrated);
void getSplinePoints(vector<Point>& dataPoint , int ptsCnt , double precision ,bool isXcalibrated);
void getControlPoints(vector<Point>& dataPoint , int ptsCnt);

#ifdef __cplusplus
}
#endif

#endif
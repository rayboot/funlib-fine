#include "curve.h"

vector<Point> splinePoint;
vector<Point> controlPoint;
int getImageLevel(vector<Point>& pts, int ptsCnt , uint8_t* level , double precision ,bool isXcalibrated){
	
	for (int i = 0; i < pts[0].X; i++)
		level[i] = (uint8_t)pts[0].Y;
	
	for (int i = pts[ptsCnt - 1].X; i < 256; i++)
        level[i] = (uint8_t)pts[ptsCnt - 1].Y;
	
	getSplinePoints(pts , ptsCnt ,precision ,isXcalibrated);
	for (int i = 0; i < splinePoint.size(); i++)
            {
                int n = splinePoint[i].Y;
                if (n < 0) n = 0;
                if (n > 255) n = 255;
                level[pts[0].X + i] = (uint8_t)n;
            }
            
	return 0;
}

void getSplinePoints(vector<Point>& dataPoint , int ptsCnt , double precision ,bool isXcalibrated)
        {
            splinePoint.clear();
            controlPoint.clear();
            if (ptsCnt == 1) splinePoint.push_back(dataPoint[0]);

            if (ptsCnt == 2)
            {
                int n = 1;
                if(isXcalibrated)
                    n=(int)((dataPoint[1].X - dataPoint[0].X) / precision);
                else n = (int)((dataPoint[1].Y - dataPoint[0].Y) / precision);
                if (n == 0) n = 1;
                if (n < 0) n = -n;
                for (int j = 0; j < n; j++)
                {
                    double t = (double)j / (double)n;

					Point pt;
					pt.X = (1 -t )*dataPoint[0].X + t*dataPoint[1].X;
					pt.Y = (1 -t )*dataPoint[0].Y + t*dataPoint[1].Y;
                    splinePoint.push_back(pt);
                }
            }

            if (ptsCnt > 2)
            {
                getControlPoints(dataPoint,ptsCnt);

                //draw bezier curves using Bernstein Polynomials
                for (int i = 0; i < controlPoint.size() - 1; i++)
                {
                    Point b1;
                    b1.X = controlPoint[i].X * 2.0 / 3.0 + controlPoint[i + 1].X / 3.0;
                    b1.Y = controlPoint[i].Y * 2.0 / 3.0 + controlPoint[i + 1].Y / 3.0;
                    Point b2;
                    b2.X  = controlPoint[i].X / 3.0 + controlPoint[i + 1].X * 2.0 / 3.0;
                    b2.Y  = controlPoint[i].Y / 3.0 + controlPoint[i + 1].Y * 2.0 / 3.0;

                    int n = 1;
                    if(isXcalibrated)
                        n=(int)((dataPoint[i + 1].X - dataPoint[i].X) / precision);
                    else n = (int)((dataPoint[i + 1].Y - dataPoint[i].Y) / precision);
                    if (n == 0) n = 1;
                    if (n < 0) n = -n;
                    for (int j = 0; j < n; j++ )
                    {
                        double t = (double)j / (double)n;
                        Point v ;
                        v.X = (1 - t) * (1 - t) * (1 - t) * dataPoint[i].X + 3 * (1 - t) * (1 - t) * t * b1.X +
                            3 * (1 - t) * t * t * b2.X + t * t * t * dataPoint[i + 1].X;
                        v.Y = (1 - t) * (1 - t) * (1 - t) * dataPoint[i].Y + 3 * (1 - t) * (1 - t) * t * b1.Y +
                            3 * (1 - t) * t * t * b2.Y + t * t * t * dataPoint[i + 1].Y;
                        splinePoint.push_back(v);
                    }
                }
            }
        }

         void getControlPoints(vector<Point>& dataPoint , int ptsCnt)
        {
            if (ptsCnt == 3)
            {
            controlPoint.push_back(dataPoint[0]);
            Point pt;
            pt.X = (6 * dataPoint[1].X - dataPoint[0].X - dataPoint[2].X)/4;
            pt.Y = (6 * dataPoint[1].Y - dataPoint[0].Y - dataPoint[2].Y)/4;
            controlPoint.push_back(pt);
            controlPoint.push_back(dataPoint[2]);
            }

            if (ptsCnt > 3)
            {
                int n = ptsCnt;
                double *diag = new double(n); // tridiagonal matrix a(i , i)
                double *sub = new double(n); // tridiagonal matrix a(i , i-1)
                double *sup = new double(n); // tridiagonal matrix a(i , i+1)

                for (int i = 0; i < n; i++)
                {
                    controlPoint.push_back(dataPoint[i]);
                    diag[i] = 4;
                    sub[i] = 1;
                    sup[i] = 1;
                }

                controlPoint[1].X = 6 * controlPoint[1].X - controlPoint[0].X;
                controlPoint[1].Y = 6 * controlPoint[1].Y - controlPoint[0].Y;
                controlPoint[n - 2].X = 6 * controlPoint[n - 2].X - controlPoint[n - 1].X;
                controlPoint[n - 2].Y = 6 * controlPoint[n - 2].Y - controlPoint[n - 1].Y;

                for (int i = 2; i < n - 2; i++)
                {
                    controlPoint[i].X = 6 * controlPoint[i].X;
                    controlPoint[i].Y = 6 * controlPoint[i].Y;
                }

                // Gaussian elimination fron row 1 to n-2
                for (int i = 2; i < n - 1; i++)
                {
                    sub[i] = sub[i] / diag[i - 1];
                    diag[i] = diag[i] - sub[i] * sup[i - 1];
                    controlPoint[i].X = controlPoint[i].X - sub[i] * controlPoint[i - 1].X;
                    controlPoint[i].Y = controlPoint[i].Y - sub[i] * controlPoint[i - 1].Y;
                }

                controlPoint[n - 2].X = controlPoint[n - 2].X / diag[n - 2];
                controlPoint[n - 2].Y = controlPoint[n - 2].Y / diag[n - 2];

                for (int i = n - 3; i >0; i--)
                {
                    controlPoint[i].X = (controlPoint[i].X - sup[i] * controlPoint[i + 1].X) / diag[i];
                    controlPoint[i].Y = (controlPoint[i].Y - sup[i] * controlPoint[i + 1].Y) / diag[i];
                }
                
                free(diag);
                free(sub);
                free(sup);
            }
        }
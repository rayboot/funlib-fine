#include <stdio.h>    
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "--imagefilter--"

#ifdef __DEBUG__
#define LOGE(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGE(...)
#endif

#define max(a,b) (a>b)?(a):(b)
#define min(a,b) (a>b)?(b):(a) 

static __inline__ int ps_normalFun(int src, int mask , float maskAlpha){
	
	if(mask == 0) 
		return src;
	else
		return ((int) (maskAlpha * src + (1 - maskAlpha) * mask));
}

static __inline__ int ps_lightenFun(int src, int mask , float maskAlpha){
	src = mask > src ? mask : src;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_darkenFun(int src, int mask , float maskAlpha) {
	src = mask > src ? src : mask;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_multiplyFun(int src, int mask , float maskAlpha) {
	
	src = mask * src / 255;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_averageFun(int src, int mask , float maskAlpha) {
	src = (src + mask) >> 1;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_addFun(int src, int mask , float maskAlpha) {
	src = (src + mask > 255) ? 255 : src + mask;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_subtractFun(int src, int mask , float maskAlpha) {
	src = (src + mask < 255) ? 0 : (src + mask - 255);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_differenceFun(int src, int mask , float maskAlpha) {
	src = (src > mask) ? (src - mask) : (mask - src);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_negationFun(int src, int mask , float maskAlpha) {
	src = (src + mask > 255) ? (src + mask) : (255 + 255 - src - mask);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_screenFun(int src, int mask , float maskAlpha) {
	src = 255 - (((255 - src) * (255 - mask)) >> 8);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_ExclusionFun(int src, int mask , float maskAlpha) {
	src = ((int)(src + mask - 2 * src * mask / 255));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_overlayFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask < 128) ? (2 * src * mask / 255) : (255 - 2
				* (255 - src) * (255 - mask) / 255)));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_softlightFun(int src, int mask , float maskAlpha) {

	src = (int) (((src < 128) ? (2 * ((mask >> 1) + 64))
				* ((float) src / 255) : (255 - (2 * (255 - ((mask >> 1) + 64))
				* (float) (255 - src) / 255))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_hardlightFun(int src, int mask , float maskAlpha) {
	return (ps_overlayFun(mask, src , maskAlpha));
}

static __inline__ int ps_ColorDodgeFun(int src, int mask , float maskAlpha) {

	src = ((int) ((mask == 255) ? mask : min(255,
				((src << 8) / (255 - mask)))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_ColorBurnFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask == 0) ? mask : max(0,
				(255 - ((255 - src) << 8) / mask))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_LinearDodgeFun(int src, int mask , float maskAlpha) {
	return ps_addFun(src, mask,maskAlpha);
}

static __inline__ int ps_LinearBurnFun(int src, int mask , float maskAlpha) {
	return ps_subtractFun(src, mask,maskAlpha);
}

static __inline__ int ps_LinearLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_LinearBurnFun(src, (2 * mask),maskAlpha)
				: ps_LinearDodgeFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_VividLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_ColorBurnFun(src, (2 * mask),maskAlpha)
				: ps_ColorDodgeFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_PinLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_darkenFun(src, (2 * mask),maskAlpha)
				: ps_lightenFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_HardMixFun(int src, int mask , float maskAlpha) {
	return ((int) ((ps_VividLightFun(src, mask,maskAlpha) < 128) ? 0 : 255));
}

static __inline__ int ps_ReflectFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask == 255) ? mask : min(255,
				(src * src / (255 - mask)))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_GlowFun(int src, int mask , float maskAlpha) {
	return ps_ReflectFun(mask, src,maskAlpha);
}

static __inline__ int ps_PhoenixFun(int src, int mask , float maskAlpha) {
	src = ((int) (min(src, mask) - max(src, mask) + 255));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_DevideFun(int src, int mask , float maskAlpha) {
	src = min(255, (int)(src/((float)(mask)/255)));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

/*
enum EffectType {
		ChannelBlend_Normal=0, 
		ChannelBlend_Lighten, 
		ChannelBlend_Darken, 
		ChannelBlend_Multiply,
		ChannelBlend_Average,
		ChannelBlend_Add,
		ChannelBlend_Subtract,
		ChannelBlend_Difference,
		ChannelBlend_Negation,
		ChannelBlend_Screen,
		ChannelBlend_Exclusion,
		ChannelBlend_Overlay,
		ChannelBlend_SoftLight,
		ChannelBlend_HardLight,
		ChannelBlend_ColorDodge,
		ChannelBlend_ColorBurn,
		ChannelBlend_LinearDodge,
		ChannelBlend_LinearBurn,
		ChannelBlend_LinearLight,
		ChannelBlend_VividLight,
		ChannelBlend_PinLight,
		ChannelBlend_HardMix,
		ChannelBlend_Reflect,
		ChannelBlend_Glow,
		ChannelBlend_Phoenix,
		ChannelBlend_Devide,
	};
	
typedef int(*EffectFun)(int src , int mask , float maskAlpha);
EffectFun effectFun_options[] = {
	&ps_normalFun,
	&ps_lightenFun,
	&ps_darkenFun,
	&ps_multiplyFun,
	&ps_averageFun,
	&ps_addFun,
	&ps_subtractFun,
	&ps_differenceFun,
	&ps_negationFun,
	&ps_screenFun,
	&ps_ExclusionFun,
	&ps_overlayFun,
	&ps_softlightFun,
	&ps_hardlightFun,
	&ps_ColorDodgeFun,
	&ps_ColorBurnFun,
	&ps_LinearDodgeFun,
	&ps_LinearBurnFun,
	&ps_LinearLightFun,
	&ps_VividLightFun,
	&ps_PinLightFun,
	&ps_HardMixFun,
	&ps_ReflectFun,
	&ps_GlowFun,
	&ps_PhoenixFun,
	&ps_DevideFun
};
*/

int clam(int value){

	value = max(0,value);
	value = min(255,value);
	return value;
}

int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffect1(    
        JNIEnv* env, jobject obj , jintArray srcBuf, jintArray matrixBuf , int w , int h) { 
        
    jint *srcTmpBuf = (*env)->GetIntArrayElements(env , srcBuf, 0);  
    jint *matrixTmpBuf = (*env)->GetIntArrayElements(env , matrixBuf, 0);   
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a;
    int r,g,b;
    int nr,ng,nb;
    
    if (srcTmpBuf == NULL) {    
        return -1;  
    }
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];    
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;
				            
    			a = matrixTmpBuf[0] * a + matrixTmpBuf[1] * r + matrixTmpBuf[2] * g + matrixTmpBuf[3] * b + matrixTmpBuf[4] * 255;
    			r = matrixTmpBuf[5] * a + matrixTmpBuf[6] * r + matrixTmpBuf[7] * g + matrixTmpBuf[8] * b + matrixTmpBuf[9] * 255;
    			g = matrixTmpBuf[10] * a + matrixTmpBuf[11] * r + matrixTmpBuf[12] * g + matrixTmpBuf[13] * b + matrixTmpBuf[14] * 255;
    			b = matrixTmpBuf[15] * a + matrixTmpBuf[16] * r + matrixTmpBuf[17] * g + matrixTmpBuf[18] * b + matrixTmpBuf[19] * 255;
				srcTmpBuf[index] =  a << 24 | r << 16 | g << 8 | b; 
			}
	}

    (*env)->ReleaseIntArrayElements(env , srcBuf, srcTmpBuf, 0);
    (*env)->ReleaseIntArrayElements(env , matrixBuf, matrixTmpBuf, 0);       
    return 0;    
}
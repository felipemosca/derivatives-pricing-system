#include "Option.cuh"
#include <math.h>

// Função normal cumulativa (aproximação rápida)
__device__ float norm_cdf(float x) {
    return 0.5f * erfcf(-x * M_SQRT1_2);
}

// Kernel CUDA para precificação Black-Scholes de várias opções
__global__ void priceOptionsBlackScholes(const Option* options, float* prices, int n) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= n) return;
    Option opt = options[idx];

    float S = opt.S;
    float K = opt.K;
    float T = opt.T;
    float r = opt.r;
    float sigma = opt.sigma;
    int isCall = opt.isCall;

    float d1 = (logf(S/K) + (r + 0.5f*sigma*sigma)*T) / (sigma*sqrtf(T));
    float d2 = d1 - sigma*sqrtf(T);

    float call = S * norm_cdf(d1) - K * expf(-r*T) * norm_cdf(d2);
    float put  = K * expf(-r*T) * norm_cdf(-d2) - S * norm_cdf(-d1);

    prices[idx] = isCall ? call : put;
}

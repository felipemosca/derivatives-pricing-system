#ifndef OPTION_CUH
#define OPTION_CUH

#include <cuda_runtime.h>

// Estrutura básica de uma opção plain vanilla
struct Option {
    float S;      // Spot price
    float K;      // Strike price
    float T;      // Time to maturity (in years)
    float r;      // Risk-free rate
    float sigma;  // Volatility
    int isCall;   // 1 = Call, 0 = Put
};

// Kernel para precificação Black-Scholes de várias opções
__global__ void priceOptionsBlackScholes(const Option* options, float* prices, int n);

#endif // OPTION_CUH

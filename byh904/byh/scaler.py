from torch import Tensor


def transform(data: Tensor, min_data: float, max_data: float) -> Tensor:
    return (data - min_data) / (max_data - min_data)

def inverse_transform(data: Tensor, min_data: float, max_data: float) -> Tensor:
    return data * (max_data - min_data) + min_data

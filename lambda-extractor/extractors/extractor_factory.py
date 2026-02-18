from .color_extractor_parallel import ColorExtractorParallel

class ExtractorFactory:
    _extractors = {
        'ver1': ColorExtractorParallel
    }

    @classmethod
    def create(cls, type, model_path):
        extractor_class = cls._extractors.get(type)
        print(f"현재 사용하는 색상 추출기 : {extractor_class}")
        if not extractor_class:
            raise ValueError(f"지원하지 않는 색상 추출기: {type}")
        return extractor_class(model_path)

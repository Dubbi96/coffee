package com.coffee.atom.service;

import com.coffee.atom.domain.area.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaRepository areaRepository;


}

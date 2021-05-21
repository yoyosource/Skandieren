package de.yoyosource.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class TypeComposition {
    private final List<Type> typeList;

    @Override
    public String toString() {
        return typeList.stream().map(Enum::toString).collect(Collectors.joining());
    }
}

package com.example.snowisland.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Maps DB faction strings to {@link Skill.Faction}; empty or unknown values default to 平民
 * so legacy rows (e.g. after ddl-auto added column) do not break reads.
 */
@Converter(autoApply = false)
public class SkillFactionConverter implements AttributeConverter<Skill.Faction, String> {

    @Override
    public String convertToDatabaseColumn(Skill.Faction attribute) {
        if (attribute == null) {
            return Skill.Faction.平民.name();
        }
        return attribute.name();
    }

    @Override
    public Skill.Faction convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Skill.Faction.平民;
        }
        String trimmed = dbData.trim();
        if (trimmed.isEmpty()) {
            return Skill.Faction.平民;
        }
        try {
            return Skill.Faction.valueOf(trimmed);
        } catch (IllegalArgumentException ex) {
            return Skill.Faction.平民;
        }
    }
}

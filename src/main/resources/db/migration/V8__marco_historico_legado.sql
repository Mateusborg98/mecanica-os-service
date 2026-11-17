-- Do not invent transitions or timestamps that were never recorded.
-- Existing orders begin with an explicit snapshot; subsequent events are journaled transactionally.
INSERT INTO historico_os(ordem_id,tipo,event_id,origem,status_anterior,status_atual,ocorrido_em)
SELECT id,'MARCO_LEGADO',id,'MIGRACAO',NULL,status,NULL FROM ordem_de_servico_jpa_entity
ON CONFLICT DO NOTHING;

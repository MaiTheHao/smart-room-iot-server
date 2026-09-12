import { mapConditionsForReplace, createOrderedItemStateManager } from '../../../common/smart_system_util.js';

export const StateManager = createOrderedItemStateManager({
  orderKey: 'sortOrder',
  mapPayload: mapConditionsForReplace,
  itemAlias: 'Condition',
});

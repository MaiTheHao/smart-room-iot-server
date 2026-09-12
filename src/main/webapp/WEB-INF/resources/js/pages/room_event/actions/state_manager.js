import { mapActionsForReplace, createOrderedItemStateManager } from '../../../common/smart_system_util.js';

export const StateManager = createOrderedItemStateManager({
  orderKey: 'executionOrder',
  mapPayload: mapActionsForReplace,
  itemAlias: 'Action',
});

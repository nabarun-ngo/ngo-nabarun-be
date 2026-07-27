import { GetPublicWorkflowFormDefinitionHandler } from './get-public-workflow-form-definition.handler';
import { GetPublicWorkflowFormDefinitionQuery } from './get-public-workflow-form-definition.query';
import { CustomFieldType } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/entity-type.enum';

describe('GetPublicWorkflowFormDefinitionHandler', () => {
  it('loads workflow start form via facades', async () => {
    const workflowFacade = {
      getDefinition: jest.fn().mockResolvedValue({
        id: 'CONTACT_REQUEST',
        version: 1,
        name: 'Contact',
        elements: [
          { id: 'start', type: 'startEvent', formKey: 'CONTACT_REQUEST:request' },
        ],
        flows: [],
      }),
    };
    const customFormsFacade = {
      getPublishedFormByKey: jest.fn().mockResolvedValue({
        id: 'form-1',
        label: 'Contact',
        description: null,
        fields: [
          {
            id: 'f1',
            formId: 'form-1',
            key: 'email',
            label: 'Email',
            fieldType: CustomFieldType.Email,
            mandatory: true,
            fieldOptions: [],
            isHidden: false,
            isEncrypted: false,
            enabled: true,
            sortOrder: 0,
            stepId: null,
            stepName: null,
            condition: null,
            dependentOptions: null,
            validationRules: null,
            viewPermissions: [],
            createdAt: new Date(),
            updatedAt: null,
          },
        ],
      }),
    };
    const options = {
      publicWorkflows: { contact: { definitionId: 'CONTACT_REQUEST' } },
    };

    const handler = new GetPublicWorkflowFormDefinitionHandler(
      workflowFacade as never,
      customFormsFacade as never,
      options,
    );

    const result = await handler.execute(new GetPublicWorkflowFormDefinitionQuery('contact'));

    expect(workflowFacade.getDefinition).toHaveBeenCalledWith('CONTACT_REQUEST');
    expect(customFormsFacade.getPublishedFormByKey).toHaveBeenCalledWith(
      EntityType.Workflow,
      'CONTACT_REQUEST:request',
    );
    expect(result.id).toBe('contact');
    expect(result.fields[0]?.key).toBe('email');
  });
});

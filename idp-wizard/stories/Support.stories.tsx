import React, { ComponentProps } from 'react';
import { Support } from '@app/Support/Support';
import { Story } from '@storybook/react';

//👇 This default export determines where your story goes in the story list
export default {
  title: 'Components/Support',
  component: Support,
};

//👇 We create a “template” of how args map to rendering
const Template: Story<ComponentProps<typeof Support>> = (args) => <Support {...args} />;

export const SupportStory = Template.bind({});
SupportStory.args = {
  /*👇 The args you need here will depend on your component */
};

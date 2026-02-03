import { configure } from 'enzyme';
import ReactSeventeenAdapter from '@wojtekmaj/enzyme-adapter-react-17';

configure({ adapter: new ReactSeventeenAdapter() });
